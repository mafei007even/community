package com.nowcoder.community.service;

import com.github.pagehelper.PageHelper;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.enums.DiscussPostStatus;
import com.nowcoder.community.utils.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.HtmlUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author mafei007
 * @date 2020/3/30 19:07
 */

@Service
public class DiscussPostService {

    private DiscussPostMapper discussPostMapper;

    private SensitiveFilter sensitiveFilter;

    public DiscussPostService(DiscussPostMapper discussPostMapper, SensitiveFilter sensitiveFilter) {
        this.discussPostMapper = discussPostMapper;
        this.sensitiveFilter = sensitiveFilter;
    }


    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }


    public Integer findDiscussPostRows(Integer userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public List<DiscussPost> findByPage(){
        PageHelper.startPage(2, 10);
        List<DiscussPost> discussPosts = discussPostMapper.selectAll();
        return discussPosts;
    }

    public int addDiscussPost(DiscussPost post){
        Assert.notNull(post, "帖子不能为空！");

        // 转义 避免攻击 <script>xxx</script>
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertSelective(post);
    }

    public DiscussPost findDiscussPostById(Integer postId) {
        Example example = new Example(DiscussPost.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", postId);
        criteria.andNotEqualTo("status", DiscussPostStatus.BLOCK);

        DiscussPost post = discussPostMapper.selectOneByExample(example);
        return post;
    }


    public int updateCommentCount(Integer id, Integer commentCount){
        DiscussPost post = new DiscussPost();
        post.setId(id);
        post.setCommentCount(commentCount);
        return discussPostMapper.updateByPrimaryKeySelective(post);
    }

}
