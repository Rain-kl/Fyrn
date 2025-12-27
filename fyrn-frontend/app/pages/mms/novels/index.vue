<script setup lang="ts">
import type { ColumnDef, RowSelectionState, Table } from '@tanstack/vue-table'
import type { MmsNovel, MmsNovelFile } from '~/api/models'
import { useApi } from '~/api/useApi'
import { formatToYMD } from '~/utils/date'
import { formatBytes } from '~/utils/file'

const { mmsNovelApi } = useApi()

const data = ref<MmsNovel[]>([])
const loading = ref(false)
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(10)

const filters = reactive({
  novelTitle: '' as string,
  novelAuthor: '' as string,
  status: '' as '' | '2' | '4',
})

const columns: ColumnDef<MmsNovel>[] = [
  {
    header: 'ID',
    accessorKey: 'id',
  },
  {
    header: '小说名称',
    accessorKey: 'novelTitle',
  },
  {
    header: '小说作者',
    accessorKey: 'novelAuthor',
  },
  {
    header: '状态',
    accessorKey: 'status',
  },
  {
    header: '创建时间',
    accessorKey: 'createTime',
    cell: (info) => formatToYMD(info.row.original.createTime),
  },
  {
    header: '更新时间',
    accessorKey: 'updateTime',
    cell: (info) => formatToYMD(info.row.original.updateTime),
  },
  {
    header: '操作',
    id: 'actions',
    cell: (info) => {
      return h('div', { class: 'flex gap-2' }, [
        h(resolveComponent('NButton'), {
          label: '查看文件',
          btn: 'ghost-primary',
          size: 'sm',
          onClick: () => {
            handleOpenFiles(info.row.original)
          },
        }),
      ])
    },
  },
]

const select = ref<RowSelectionState>()

const table = useTemplateRef<Table<MmsNovel>>('table')

const isFilesDialogOpen = ref(false)
const selectedNovel = ref<MmsNovel | null>(null)
const filesLoading = ref(false)
const files = ref<MmsNovelFile[]>([])

const filesColumns: ColumnDef<MmsNovelFile>[] = [
  {
    header: '文件名',
    accessorKey: 'fileName',
  },
  {
    header: '大小',
    accessorKey: 'fileSize',
    cell: (info) => formatBytes(info.row.original.fileSize),
  },
  {
    header: '字数',
    accessorKey: 'wordCount',
  },
  {
    header: '更新时间',
    accessorKey: 'updateTime',
    cell: (info) => formatToYMD(info.row.original.updateTime),
  },
]

const fetchData = async () => {
  loading.value = true
  try {
    const result = await mmsNovelApi.mmsPageGet({
      operator: 'admin',
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      novelTitle: filters.novelTitle || undefined,
      novelAuthor: filters.novelAuthor || undefined,
    })

    if (result.code === 200) {
      let rows = result.data?.rows || []
      if (filters.status !== '') {
        const s = Number(filters.status)
        rows = rows.filter(r => r.status === s)
      }
      data.value = rows
      total.value = result.data?.total || 0
    }
  } catch (error) {
    console.error('Failed to fetch novels:', error)
  } finally {
    loading.value = false
  }
}

const handleSync = async () => {
  loading.value = true
  try {
    const result = await mmsNovelApi.mmsSyncPost()
    if (result.code === 200) {
      fetchData()
    }
  } catch (error) {
    console.error('Failed to sync novels:', error)
  } finally {
    loading.value = false
  }
}

const handleOpenFiles = async (row: MmsNovel) => {
  selectedNovel.value = row
  isFilesDialogOpen.value = true
  files.value = []

  // 接口参数是 string，这里统一转 string
  const mmsNovelId = row.id == null ? undefined : String(row.id)
  if (!mmsNovelId)
    return

  filesLoading.value = true
  try {
    const result = await mmsNovelApi.mmsFileGet({ mmsNovelId })
    if (result.code === 200) {
      files.value = result.data || []
    }
  } catch (error) {
    console.error('Failed to fetch novel files:', error)
  } finally {
    filesLoading.value = false
  }
}

watch([pageNo, pageSize], () => {
  fetchData()
})

watch(filters, () => {
  // 过滤条件变化时回到第一页
  pageNo.value = 1
  fetchData()
}, { deep: true })

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="flex flex-col space-y-4">
    <!-- header -->
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div class="grid w-full gap-2 md:grid-cols-4">
        <NInput
          v-model="filters.novelTitle"
          placeholder="小说名称"
        />

        <NInput
          v-model="filters.novelAuthor"
          placeholder="小说作者"
        />

        <NSelect
          :items="['全部状态', '正常', '删除']"
          :model-value="filters.status === '' ? '全部状态' : filters.status === '2' ? '正常' : '删除'"
          @update:model-value="filters.status = $event === '正常' ? '2' : $event === '删除' ? '4' : ''"
        />
      </div>

      <div class="flex items-center gap-x-2 sm:ml-auto">
        <NButton
            label="刷新"
            btn="solid-gray"
            leading="i-radix-icons-update"
            class="w-full sm:w-auto sm:shrink-0 active:translate-y-0.5"
            :loading="loading"
            @click="fetchData"
        />

        <NButton
            label="同步"
            btn="solid-primary"
            leading="i-radix-icons-download"
            class="w-full sm:w-auto sm:shrink-0 active:translate-y-0.5"
            :loading="loading"
            @click="handleSync"
        />
      </div>
    </div>

    <!-- table -->
    <NTable
        ref="table"
        v-model:row-selection="select"
    :loading
        :columns
        :data
        enable-row-selection
        row-id="id"
    >
      <!-- cells -->
      <template #status-cell="{ cell }">
        <NBadge
            :una="{
            badgeDefaultVariant: cell.row.original.status === 2
              ? 'badge-soft-success' : 'badge-soft-error' }"
            class="capitalize"
            :label="cell.row.original.status === 2 ? '正常' : '删除'"
        />
      </template>
      <!-- end cell -->
    </NTable>

    <!-- footer -->
    <div
        class="flex items-center justify-between px-2"
    >
      <div
          class="hidden text-sm text-muted sm:block"
      >
        已选择 {{ table?.getFilteredSelectedRowModel().rows.length.toLocaleString() }} / {{ total.toLocaleString() }} 条记录
      </div>
      <div class="flex items-center space-x-6 lg:space-x-8">
        <div
            class="hidden items-center justify-center text-sm font-medium sm:flex space-x-2"
        >
          <span class="text-nowrap">
            每页行数
          </span>

          <NSelect
              :items="[10, 20, 30, 40, 50]"
              :_select-trigger="{
              class: 'w-15',
            }"
              :model-value="pageSize"
              @update:model-value="pageSize = $event as unknown as number"
          />
        </div>

        <div
            class="flex items-center justify-center text-sm font-medium"
        >
          第 {{ pageNo }} 页，共 {{ Math.ceil(total / pageSize) }} 页
        </div>

        <NPagination
            :page="pageNo"
            :total="total"
            :show-list-item="false"
            :items-per-page="pageSize"
            @update:page="pageNo = $event"
        />
      </div>
    </div>

    <!-- files dialog -->
    <NDialog v-model:open="isFilesDialogOpen" :title="`物料文件列表：${selectedNovel?.novelTitle || ''}`">
      <div class="space-y-3">
        <div class="text-sm text-muted">
          小说ID：{{ selectedNovel?.id }}
        </div>

        <div v-if="filesLoading" class="text-sm">
          加载中...
        </div>

        <div v-else>
          <div v-if="files.length === 0" class="text-sm text-muted">
            暂无文件
          </div>

          <div v-else class="overflow-auto">
            <NTable
              :loading="filesLoading"
              :columns="filesColumns"
              :data="files"
              row-id="id"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <NButton label="关闭" btn="solid-gray" @click="isFilesDialogOpen = false" />
        </div>
      </template>
    </NDialog>
  </div>
</template>
