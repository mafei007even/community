package com.nowcoder.community.service;

import com.github.pagehelper.PageHelper;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.enums.DiscussPostStatus;
import com.nowcoder.community.model.enums.DiscussPostType;
import com.nowcoder.community.model.enums.OrderMode;
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


    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit, OrderMode orderMode){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
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

    public DiscussPost findDiscussPostByIdAllowBlock(Integer postId) {
        return discussPostMapper.selectByPrimaryKey(postId);
    }

    public int updateCommentCount(Integer id, Integer commentCount){
        DiscussPost post = new DiscussPost();
        post.setId(id);
        post.setCommentCount(commentCount);
        return discussPostMapper.updateByPrimaryKeySelective(post);
    }

    public int updateType(Integer postId, DiscussPostType type) {
        Assert.notNull(postId, "帖子postId不能为空！");
        Assert.notNull(type, "帖子type不能为空！");
        DiscussPost post = new DiscussPost();
        post.setId(postId);
        post.setType(type);
        return discussPostMapper.updateByPrimaryKeySelective(post);
    }

    public int updateStatus(Integer postId, DiscussPostStatus status) {
        Assert.notNull(postId, "帖子postId不能为空！");
        Assert.notNull(status, "帖子status不能为空！");
        DiscussPost post = new DiscussPost();
        post.setId(postId);
        post.setStatus(status);
        return discussPostMapper.updateByPrimaryKeySelective(post);
    }

    public int updateScore(Integer postId, double score) {
        Assert.notNull(postId, "帖子postId不能为空！");
        Assert.notNull(score, "帖子score不能为空！");
        DiscussPost post = new DiscussPost();
        post.setId(postId);
        post.setScore(score);
        return discussPostMapper.updateByPrimaryKeySelective(post);
    }

}
