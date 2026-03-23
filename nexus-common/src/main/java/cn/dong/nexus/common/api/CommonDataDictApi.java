package cn.dong.nexus.common.api;

import cn.dong.nexus.common.domain.bo.DataDictBO;

import java.util.List;

public interface CommonDataDictApi {

    /**
     * 获取字典项
     */
    List<DataDictBO> getDataDictItems(List<String> itemIds);
}
