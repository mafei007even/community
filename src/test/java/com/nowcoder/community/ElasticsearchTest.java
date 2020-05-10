package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.elasticsearch.model.EsDiscussPost;
import com.nowcoder.community.elasticsearch.repo.DiscussPostRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * @author mafei007
 * @date 2020/5/10 17:54
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommunityApplication.class)
public class ElasticsearchTest {

	@Autowired
	private DiscussPostMapper discussPostMapper;

	@Autowired
	private DiscussPostRepository discussPostRepository;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Test
	public void testInsert(){
	}

	@Test
	public void testSave(){
		List<EsDiscussPost> esDiscussPosts = discussPostMapper.selectAllDiscussPostsForEs();
		discussPostRepository.saveAll(esDiscussPosts);
	}

	@Test
	public void testSearchByRepository(){
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
				.withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
				// Elasticsearch中的分页是从第0页开始
				.withPageable(PageRequest.of(0, 10))
				.withHighlightFields(
						new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
						new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
				).build();

		/**
		 * 打印输出发现并没有高亮
		 * discussPostRepository.search(searchQuery); 底层调用:
		 * 		elasticTemplate.queryForPage(searchQuery, class, SearchResultMapper)
		 * 	底层获取得到了高亮显示的值， 但是没有返回.
		 */
		Page<EsDiscussPost> page = discussPostRepository.search(searchQuery);
		System.out.println(page.getTotalElements());
		System.out.println(page.getTotalPages());
		System.out.println(page.getNumber());
		System.out.println(page.getSize());

		page.forEach(System.out::println);

	}

	@Test
	public void testSearchByTemplate() {

		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
				.withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
				// Elasticsearch中的分页是从第0页开始
				.withPageable(PageRequest.of(0, 10))
				.withHighlightFields(
						new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
						new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
				).build();

		// 从 es 中查到的结果会交给 SearchResultMapper 处理，通过SearchResponse得到查到的值
		AggregatedPage<EsDiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, EsDiscussPost.class, new SearchResultMapper() {

			@Override
			public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
				SearchHits hits = searchResponse.getHits();
				// 有没有命中数据，就是有没有查到
				if (hits.getTotalHits() <= 0) {
					return null;
				}

				// 封装结果集，主要是加上高亮
				List<EsDiscussPost> list = new ArrayList<>();

				for (SearchHit hit : hits) {
					EsDiscussPost post = new EsDiscussPost();

					// _source 就是存储的内容
					Map<String, Object> sourceMap = hit.getSourceAsMap();

					String id = sourceMap.get("id").toString();
					post.setId(Integer.parseInt(id));

					String userId = sourceMap.get("userId").toString();
					post.setUserId(Integer.parseInt(userId));

					String status = sourceMap.get("status").toString();
					post.setStatus(Integer.parseInt(status));

					String type = sourceMap.get("type").toString();
					post.setType(Integer.parseInt(type));

					String score = sourceMap.get("score").toString();
					post.setScore(Double.parseDouble(score));

					String commentCount = sourceMap.get("commentCount").toString();
					post.setCommentCount(Integer.parseInt(commentCount));

					// 有可能查询的关键字在 title 中不存在
					String title = sourceMap.get("title").toString();
					post.setTitle(title);

					// 有可能查询的关键字在 content 中不存在
					String content = sourceMap.get("content").toString();
					post.setContent(content);

					String createTime = sourceMap.get("createTime").toString();
					post.setCreateTime(new Date(Long.parseLong(createTime)));

					// 处理高亮显示
					// 如果存在就覆盖掉之前没有设置高亮的
					HighlightField titleField = hit.getHighlightFields().get("title");
					if (titleField != null) {
						// 搜互联网寒冬，有可能title中匹配了多个？？
						post.setTitle(titleField.getFragments()[0].toString());

						Arrays.stream(titleField.getFragments()).forEach(System.out::println);
					}

					HighlightField contentField = hit.getHighlightFields().get("content");
					if (contentField != null) {
						// 搜互联网寒冬，有可能content中匹配了多个？？
						post.setContent(contentField.getFragments()[0].toString());

						Arrays.stream(contentField.getFragments()).forEach(System.out::println);
					}
					System.out.println("=========================");

					list.add(post);
				}

				return new AggregatedPageImpl(list, pageable,
						hits.getTotalHits(), searchResponse.getAggregations(),
						searchResponse.getScrollId(), hits.getMaxScore());
			}

			@Override
			public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
				return null;
			}
		});

		System.out.println(page.getTotalElements());
		System.out.println(page.getTotalPages());
		System.out.println(page.getNumber());
		System.out.println(page.getSize());

		// 返回的 title、content 值都只是匹配的那一段，不是全部数据
		page.forEach(System.out::println);
	}
}
