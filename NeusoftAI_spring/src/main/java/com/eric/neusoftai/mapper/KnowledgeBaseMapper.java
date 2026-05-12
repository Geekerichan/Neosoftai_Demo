package com.eric.neusoftai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eric.neusoftai.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    @Update("UPDATE knowledge_base SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementView(@Param("id") Long id);

    @Update("UPDATE knowledge_base SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLike(@Param("id") Long id);
}
