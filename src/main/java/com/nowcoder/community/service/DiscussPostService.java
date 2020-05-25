package com.nowcoder.community.service;

import com.github.pagehelper.PageHelper;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.enums.DiscussPostStatus;
import com.nowcoder.community.model.enums.DiscussPostType;
import com.nowcoder.community.model.enums.OrderMode;
import com.nowcoder.community.utils.CacheUtils;
import com.nowcoder.community.utils.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DiscussPostService {

    private final DiscussPostMapper discussPostMapper;
    private final SensitiveFilter sensitiveFilter;

    public DiscussPostService(DiscussPostMapper discussPostMapper, SensitiveFilter sensitiveFilter) {
        this.discussPostMapper = discussPostMapper;
        this.sensitiveFilter = sensitiveFilter;
    }

    /**
     * 调用地方：
     *      访问首页按时间排序
     *      按热门贴排序
     *      查看用户的帖子
     * 缓存只加在热门帖子页面，因为热门帖子是由定时调度来更新的，页面变化频率较低，可以做本地缓存
     * 有影响的地方就是显示的评论数量可能会不正确，但用户点击帖子详情页时会查询，这时候评论数也就对了，这里加缓存不会很影响用户体验
     * <p>
     * 还有个影响就是如果加精、置顶、删除、恢复了从最热页面查看是没有的，但从最新页面查看却能看到，对页面影响很大，这里不太好
     * 所以在加精、置顶、删除、恢复时清除缓存
     *
     * 访问首页按时间排序的话就不适合做缓存
     *
     * @param userId 0是查全部帖子，否则就是查指定 userId 的帖子
     * @param offset
     * @param limit
     * @param orderMode
     * @return
     */
    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit, OrderMode orderMode){
        if (userId == 0 && orderMode == OrderMode.HEAT) {
            // offset + ":" + limit 是唯一的，可以作为缓存的 key
            return CacheUtils.POST_LIST_CACHE.get(offset + ":" + limit);
        }
        log.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * 查询帖子数量，此数据对分页有影响，
     * 对查询所有帖子时做本地缓存
     * 新发的帖子就不会被计算进去，分页数不太对也没关系，等下次缓存过期了刷新时校正过来就好
     * @param userId 0就是查所有的帖子数，否则就是查指定 userId 的帖子数
     * @return
     */
    public Integer findDiscussPostRows(Integer userId){
        if (userId == 0) {
            // key 就永远是 0
            return CacheUtils.POST_ROWS_CACHE.get(userId);
        }
        log.debug("load post rows from DB.");
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
