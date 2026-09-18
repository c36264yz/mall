package com.macro.mall.service;

import com.macro.mall.dao.CmsPrefrenceAreaProductRelationDao;
import com.macro.mall.dao.CmsSubjectProductRelationDao;
import com.macro.mall.dao.PmsMemberPriceDao;
import com.macro.mall.dao.PmsProductAttributeValueDao;
import com.macro.mall.dao.PmsProductFullReductionDao;
import com.macro.mall.dao.PmsProductLadderDao;
import com.macro.mall.dao.PmsProductVertifyRecordDao;
import com.macro.mall.dao.PmsSkuStockDao;
import com.macro.mall.dto.PmsProductParam;
import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.CmsPrefrenceAreaProductRelation;
import com.macro.mall.model.CmsSubjectProductRelation;
import com.macro.mall.model.PmsMemberPrice;
import com.macro.mall.model.PmsProductAttributeValue;
import com.macro.mall.model.PmsProductFullReduction;
import com.macro.mall.model.PmsProductLadder;
import com.macro.mall.model.PmsProductVertifyRecord;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.service.impl.PmsProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PmsProductServiceImplTest {

    @Mock
    private PmsProductMapper productMapper;
    @Mock
    private PmsMemberPriceDao memberPriceDao;
    @Mock
    private PmsProductLadderDao productLadderDao;
    @Mock
    private PmsProductFullReductionDao productFullReductionDao;
    @Mock
    private PmsSkuStockDao skuStockDao;
    @Mock
    private PmsProductAttributeValueDao productAttributeValueDao;
    @Mock
    private CmsSubjectProductRelationDao subjectProductRelationDao;
    @Mock
    private CmsPrefrenceAreaProductRelationDao prefrenceAreaProductRelationDao;
    @Mock
    private PmsProductVertifyRecordDao productVertifyRecordDao;

    private PmsProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new PmsProductServiceImpl();
        ReflectionTestUtils.setField(productService, "productMapper", productMapper);
        ReflectionTestUtils.setField(productService, "memberPriceDao", memberPriceDao);
        ReflectionTestUtils.setField(productService, "productLadderDao", productLadderDao);
        ReflectionTestUtils.setField(productService, "productFullReductionDao", productFullReductionDao);
        ReflectionTestUtils.setField(productService, "skuStockDao", skuStockDao);
        ReflectionTestUtils.setField(productService, "productAttributeValueDao", productAttributeValueDao);
        ReflectionTestUtils.setField(productService, "subjectProductRelationDao", subjectProductRelationDao);
        ReflectionTestUtils.setField(productService, "prefrenceAreaProductRelationDao", prefrenceAreaProductRelationDao);
        ReflectionTestUtils.setField(productService, "productVertifyRecordDao", productVertifyRecordDao);
    }

    @Test
    void createInsertsProductAndAllRelationLists() {
        PmsProductParam param = productParam();

        int count = productService.create(param);

        assertThat(count).isEqualTo(1);
        verify(productMapper).insertSelective(param);
        verify(memberPriceDao).insertList(anyList());
        verify(productLadderDao).insertList(anyList());
        verify(productFullReductionDao).insertList(anyList());
        verify(skuStockDao).insertList(anyList());
        verify(productAttributeValueDao).insertList(anyList());
        verify(subjectProductRelationDao).insertList(anyList());
        verify(prefrenceAreaProductRelationDao).insertList(anyList());
    }

    @Test
    void updateVerifyStatusUpdatesProductsAndCreatesVerifyRecords() {
        when(productMapper.updateByExampleSelective(any(), any())).thenReturn(2);

        int count = productService.updateVerifyStatus(List.of(1L, 2L), 1, "approved");

        assertThat(count).isEqualTo(2);
        ArgumentCaptor<List<PmsProductVertifyRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(productVertifyRecordDao).insertList(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue()).extracting(PmsProductVertifyRecord::getProductId).containsExactly(1L, 2L);
        assertThat(captor.getValue()).allSatisfy(record -> {
            assertThat(record.getStatus()).isEqualTo(1);
            assertThat(record.getDetail()).isEqualTo("approved");
        });
    }

    private static PmsProductParam productParam() {
        PmsProductParam param = new PmsProductParam();
        param.setName("product");
        param.setProductSn("P001");

        PmsSkuStock stock = new PmsSkuStock();
        stock.setSkuCode("SKU-001");
        stock.setProductId(1L);

        param.setMemberPriceList(List.of(new PmsMemberPrice()));
        param.setProductLadderList(List.of(new PmsProductLadder()));
        param.setProductFullReductionList(List.of(new PmsProductFullReduction()));
        param.setSkuStockList(List.of(stock));
        param.setProductAttributeValueList(List.of(new PmsProductAttributeValue()));
        param.setSubjectProductRelationList(List.of(new CmsSubjectProductRelation()));
        param.setPrefrenceAreaProductRelationList(List.of(new CmsPrefrenceAreaProductRelation()));
        return param;
    }
}
