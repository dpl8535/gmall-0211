package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author dplStart
 * @create 上午 11:52
 * @Description
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
