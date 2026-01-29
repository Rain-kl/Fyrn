package com.arctel.mms.service.impl;

import com.arctel.common.utils.NovelUtil;
import com.arctel.domain.dao.entity.MmsNovel;
import com.arctel.domain.dao.entity.MmsNovelFile;
import com.arctel.domain.dao.mapper.MmsNovelMapper;
import com.arctel.domain.dto.LocalFileSimpleDTO;
import com.arctel.domain.dto.input.SyncMaterialInput;
import com.arctel.domain.dto.input.UMmsPageInput;
import com.arctel.mms.service.DataSyncService;
import com.arctel.mms.service.MmsNovelFileService;
import com.arctel.mms.service.MmsNovelService;
import com.arctel.oms.biz.job.JobRunnable;
import com.arctel.oms.biz.job.ThreadPoolJobService;
import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.common.domain.OmsJob;
import com.arctel.oms.common.domain.input.CreateJobInput;
import com.arctel.oms.common.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author ryan
 * @description
 * @createDate
 */
@Service
@Slf4j
public class DataSyncServiceImpl implements DataSyncService {


    @Resource
    ThreadPoolJobService threadPoolJobService;

    @Resource
    MmsNovelFileService mmsNovelFileService;

    @Resource
    MmsNovelMapper mmsNovelMapper;

    @Resource
    MmsNovelService mmsNovelService;


    @Override
    public String fileToOss(SyncMaterialInput input) throws IOException {
        Integer size = input.getSize();
        if (size == null || size <= 0) {
            size = Integer.MAX_VALUE;
        }
        UMmsPageInput uMmsPageInput = new UMmsPageInput();
        uMmsPageInput.setPageSize(size);
        uMmsPageInput.setPageNo(1);
        Result<BaseQueryPage<LocalFileSimpleDTO>> unprocessedLocalFile = mmsNovelFileService.getUnprocessedLocalFile(uMmsPageInput);
        List<LocalFileSimpleDTO> rows = unprocessedLocalFile.getData().getRows();

        OmsJob job = threadPoolJobService.createJob(new CreateJobInput("sync_material", "同步物料到OOS"),
                new JobRunnable(threadPoolJobService) {
                    @Override
                    public void taskRun() {
                        rows.forEach(f -> {
                            try {
                                mmsNovelFileService.syncLocalFile(f, input.getOperator());
                                updateProgress(rows.size(), "Processed: " + f.getFileName());
                            } catch (Exception e) {
                                // 记录失败日志或更新 job 状态，但不中断其他文件处理
                                updateProgress(rows.size(), "Failed to process file: " + f.getFileName() + ", error: " + e.getMessage());
                            }
                        });
                    }
                });
        return job.getJobId();

    }

    @Override
    public OmsJob ossToMms() {
        return threadPoolJobService.createJob(new CreateJobInput("sync-mms", "同步umms到mms"), new JobRunnable(threadPoolJobService) {
            @Override
            protected void taskRun() {
                int pageSize = 100;
                int pageNum = 1;
                while (true) {
                    BaseQueryPage<MmsNovelFile> unlinkedMmsNovelFile = mmsNovelFileService.getUnlinkedMmsNovelFile(pageNum, pageSize);

                    if (unlinkedMmsNovelFile.getTotal() <= 0) {
                        break;
                    }
                    this.setTotalProgress(unlinkedMmsNovelFile.getTotal());

                    unlinkedMmsNovelFile.getRows().forEach(novelFile -> {
                        String fileName = novelFile.getFileName();
                        List<String> novelBasicMetadata = NovelUtil.extractTitleAndAuthor(fileName);

                        // 查询小说是否存在
                        String title = novelBasicMetadata.get(0);
                        String author = novelBasicMetadata.get(1);
                        MmsNovel mmsNovel = mmsNovelMapper.selectOne(
                                new LambdaQueryWrapper<MmsNovel>()
                                        .eq(MmsNovel::getNovelTitle, title)
                                        .eq(MmsNovel::getNovelAuthor, author)
                        );
                        // 如果小说不存在，则新增小说记录
                        if (mmsNovel == null) {
                            MmsNovel newmmsNovel = new MmsNovel();
                            newmmsNovel.setNovelTitle(title);
                            newmmsNovel.setNovelAuthor(author);
                            try {
                                mmsNovelService.createNovel(newmmsNovel, novelFile);
                                this.updateProgress("新增物料信息: " + fileName + ", 小说ID: " + novelFile.getNovelId());
                            } catch (Exception e) {
                                log.error("创建物料失败，文件名: {}", fileName, e);
                                this.updateLog("创建物料失败，文件名: " + fileName + ", 错误信息: " + e.getMessage());

                            }
                        } else {
                            novelFile.setNovelId(mmsNovel.getId());
                            mmsNovelFileService.update(novelFile,
                                    new LambdaQueryWrapper<MmsNovelFile>()
                                            .eq(MmsNovelFile::getId, novelFile.getId())
                            );
                            this.updateProgress("更新物料信息: " + fileName + ", 小说ID: " + novelFile.getNovelId());
                        }
                    });
                }
            }
        });
    }


}
