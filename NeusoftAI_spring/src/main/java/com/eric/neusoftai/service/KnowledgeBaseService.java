package com.eric.neusoftai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eric.neusoftai.entity.KnowledgeBase;
import com.eric.neusoftai.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public Page<KnowledgeBase> pageList(String keyword, String category, Integer current, Integer size) {
        Page<KnowledgeBase> page = new Page<>(current, size);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<KnowledgeBase>()
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(KnowledgeBase::getTitle, keyword)
                        .or()
                        .like(KnowledgeBase::getTags, keyword)
                        .or()
                        .like(KnowledgeBase::getContent, keyword))
                .eq(category != null && !category.isEmpty() && !"ALL".equals(category), KnowledgeBase::getCategory, category)
                .orderByDesc(KnowledgeBase::getCreateTime);
        return knowledgeBaseMapper.selectPage(page, wrapper);
    }

    public boolean add(KnowledgeBase kb) {
        if (kb.getViewCount() == null) {
            kb.setViewCount(0);
        }
        if (kb.getLikeCount() == null) {
            kb.setLikeCount(0);
        }
        return knowledgeBaseMapper.insert(kb) > 0;
    }

    public boolean update(KnowledgeBase kb) {
        return knowledgeBaseMapper.updateById(kb) > 0;
    }

    public boolean deleteById(Long id) {
        return knowledgeBaseMapper.deleteById(id) > 0;
    }

    public KnowledgeBase getById(Long id) {
        return knowledgeBaseMapper.selectById(id);
    }

    public void incrementView(Long id) {
        knowledgeBaseMapper.incrementView(id);
    }

    public void incrementLike(Long id) {
        knowledgeBaseMapper.incrementLike(id);
    }
}
