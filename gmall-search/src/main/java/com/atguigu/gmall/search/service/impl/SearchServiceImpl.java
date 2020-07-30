package com.atguigu.gmall.search.service.impl;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.*;
import com.atguigu.gmall.search.service.SearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dplStart
 * @create 上午 12:58
 * @Description
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public SearchResponseVo search(SearchParamVo searchParamVo) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        searchRequest.source(buildDSL(searchParamVo));
        try {
            //根据buildDSL()方法中获取到的dsl语句执行查询并获取到结果封装到searchResponse中
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //解析查询结果相应到前端
            SearchResponseVo searchResponseVo = parseSearchResult(searchResponse);
            // 分页信息
            searchResponseVo.setPageNum(searchParamVo.getPageNum());
            searchResponseVo.setPageSize(searchParamVo.getPageSize());
            return searchResponseVo;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * SearchSourceBuilder是构建查询语句，SearchResponse是通过查询语句获取到的结果
     * parseSearchResult（SearchResponse searchResponse）
     * 是对查询结果进行解析封装到SearchResponseVo中
     *
     * @param searchResponse
     * @return
     */
    public SearchResponseVo parseSearchResult(SearchResponse searchResponse) {
        //结果相应vo
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        //1.获取到查询结果
        SearchHits hits = searchResponse.getHits();
        searchResponseVo.setTotal(hits.totalHits);

        //1.1.获取到hits的总数赋值给总条数
        searchResponseVo.setTotal(hits.getTotalHits());

        //2.根据查询结果hits获取到hits并赋值给goods
        SearchHit[] hitsHits = hits.getHits();
        if (hitsHits == null || hitsHits.length == 0) {
            return searchResponseVo;
        }
        //2.1.遍历hitHits把遍历的值赋值给goods
        List<Goods> goodsList = Stream.of(hitsHits).map(hit -> {
            String sourceAsString = hit.getSourceAsString();
            try {
                Goods goods = MAPPER.readValue(sourceAsString, Goods.class);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();

                //2.2.获取到高亮的字段赋值给goods中的title,高亮字段获取到的是一个数组，取第一个即可
                HighlightField highlightField = highlightFields.get("title");
                goods.setTitle(highlightField.getFragments()[0].toString());
                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        searchResponseVo.setGoodsList(goodsList);

        //3.聚合是和外层hits同级所以使用SearchResponse获取到
        Aggregations aggregations = searchResponse.getAggregations();
        Map<String, Aggregation> aggregationMap = aggregations.asMap();

        //3.1.获取到brandIdAgg聚合赋值给brandEntity
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        List<BrandEntity> brands = buckets.stream().map(bucket -> {
            BrandEntity brandEntity = new BrandEntity();
            //3.1.1.获取到brandIdAgg下的key并赋值给brandEntity中的id
            Long brandId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
            brandEntity.setId(brandId);

            Aggregations subAggregationMap = ((Terms.Bucket) bucket).getAggregations();
            //3.1.3.获取到brandNameAgg聚合
            ParsedStringTerms brandNameAgg = (ParsedStringTerms) subAggregationMap.get("brandNameAgg");
            List<? extends Terms.Bucket> brandNameAggBuckets = brandNameAgg.getBuckets();
            if (!CollectionUtils.isEmpty(brandNameAggBuckets)) {
                Terms.Bucket nameBucket = brandNameAggBuckets.get(0);
                if (nameBucket != null) {
                    brandEntity.setName(nameBucket.getKeyAsString());
                }
            }

            //3.1.2.获取到brandLogo聚合
            Map<String, Aggregation> brandLogoAggMap = subAggregationMap.asMap();
            ParsedStringTerms brandLogoAgg = (ParsedStringTerms) brandLogoAggMap.get("brandLogoAgg");
            List<? extends Terms.Bucket> logoAggBuckets = brandLogoAgg.getBuckets();
            if (!CollectionUtils.isEmpty(logoAggBuckets)) {
                Terms.Bucket brandLogo = logoAggBuckets.get(0);
                brandEntity.setLogo(brandLogo.getKeyAsString());
            }

            return brandEntity;
        }).collect(Collectors.toList());
        searchResponseVo.setBrands(brands);

        //3.2.获取到categoryIdAgg聚合
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryIdAggBuckets)) {
            List<CategoryEntity> Categories = categoryIdAggBuckets.stream().map(categoryIdAggBucket -> {
                //3.2.1.给categoryEntity的id赋值
                Long categoryId = ((Terms.Bucket) categoryIdAggBucket).getKeyAsNumber().longValue();
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(categoryId);

                //3.2.2.获取到categoryNameAgg聚合并赋值给categoryEntity中的name

                ParsedStringTerms categoryNameAgg = (ParsedStringTerms) ((Terms.Bucket) categoryIdAggBucket).getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> categoryNameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(categoryNameAggBuckets)) {
                    String categoryName = categoryNameAggBuckets.get(0).getKeyAsString();
                    categoryEntity.setName(categoryName);
                    System.out.println("categoryName = " + categoryName);
                }
                return categoryEntity;
            }).collect(Collectors.toList());
            searchResponseVo.setCategories(Categories);
        }

        //3.3.获取attrAgg嵌套聚合
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)) {
            List<SearchResponseAttrVo> SearchResponseAttrVos = attrIdAggBuckets.stream().map(attrAggBucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                Long attrId = ((Terms.Bucket) attrAggBucket).getKeyAsNumber().longValue();
                searchResponseAttrVo.setAttrId(attrId);

                //3.3.1.获取到attrNameAgg
                Aggregations attrNameAggBuckets = ((Terms.Bucket) attrAggBucket).getAggregations();
                Map<String, Aggregation> attrNameAggAsMap = attrNameAggBuckets.asMap();
                if (!CollectionUtils.isEmpty(attrNameAggAsMap)) {
                    ParsedStringTerms attrNameAgg = (ParsedStringTerms) attrNameAggAsMap.get("attrNameAgg");
                    List<? extends Terms.Bucket> attrNameAggBucket = attrNameAgg.getBuckets();
                    if (!CollectionUtils.isEmpty(attrNameAggBucket)) {
                        Terms.Bucket attrName = attrNameAggBucket.get(0);
                        if (attrName != null) {
                            searchResponseAttrVo.setAttrName(attrName.getKeyAsString());
                        }
                    }
                }
                //3.3.2.获取到attrValueAgg
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) attrNameAggAsMap.get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueBucket = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrValueBucket)) {
                    List<String> attrValues = attrValueBucket.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrVo.setAttrValues(attrValues);
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            searchResponseVo.setFilters(SearchResponseAttrVos);
        }
        return searchResponseVo;
    }


    /**
     * 构建查询dsl语句
     *
     * @param paramVo
     * @return
     */
    private SearchSourceBuilder buildDSL(SearchParamVo paramVo) {

        //1.构建bool查询
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        String keyword = paramVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            return null;
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1.构建关键字查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));

        //2.1.构建过滤查询
        //2.2.1构建品牌过滤查询
        List<Long> brandId = paramVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }

        //2.2.2.构建类别过滤查询
        List<Long> cid = paramVo.getCid();
        if (!CollectionUtils.isEmpty(cid)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", cid));
        }

        //2.2.3.构建价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            boolQueryBuilder.filter(rangeQuery);
            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }
        }

        //2.2.4.库存过滤
        Boolean store = paramVo.getStore();
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }

        //2.2.5规格参数过滤：["8:8G-12G", "9:128G-256G"]
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {//遍历["8:8G-12G", "9:128G-256G"]
                String[] attr = StringUtils.split(prop, ":"); //分割8:8G-12G
                if (attr != null && attr.length == 2) {
                    //构建查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attr[0]));
                    String[] attrValues = StringUtils.split(attr[1], "-");
                    if (attrValues != null && attrValues.length > 0) {
                        boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    }
                    //嵌套查询//new了一个新的没有使用QueryBuilders构建
                    NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None);
                    boolQueryBuilder.filter(nestedQueryBuilder);
                }
            });
        }

        //3.排序
        Integer sort = paramVo.getSort();
        if (sort != null) {
            switch (sort) {
                case 1:
                    sourceBuilder.sort("price", SortOrder.ASC);
                    break;
                case 2:
                    sourceBuilder.sort("price", SortOrder.DESC);
                    break;
                case 3:
                    sourceBuilder.sort("sale", SortOrder.ASC);
                    break;
                case 4:
                    sourceBuilder.sort("createTime", SortOrder.DESC);
                    break;
            }
        }

        //4.分页
        sourceBuilder.from((paramVo.getPageNum() - 1) * paramVo.getPageSize());
        sourceBuilder.size(paramVo.getPageSize());

        //5.高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red'>").postTags("</font>"));

        //6.聚合
        //6.1.品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("brandLogoAgg").field("logo"))
        );
        //6.2.分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );
        // 6.3. 规格参数聚合//field的层级一定要写清楚
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                )
        );
        //7.构建过滤
        sourceBuilder.fetchSource(new String[]{"skuId", "title", "subTitle", "price", "image"}, null);
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }
}
