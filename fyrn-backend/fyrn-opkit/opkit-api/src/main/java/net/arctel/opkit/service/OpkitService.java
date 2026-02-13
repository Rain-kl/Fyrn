package net.arctel.opkit.service;


import net.arctel.opkit.output.OpkitListOutput;

import java.util.List;

public interface OpkitService {


    /**
     * 列出所有工具
     */
    List<OpkitListOutput> listTools();


}
