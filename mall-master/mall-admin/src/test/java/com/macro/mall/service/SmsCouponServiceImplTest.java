package com.macro.mall.service;

import com.macro.mall.dao.SmsCouponDao;
import com.macro.mall.dao.SmsCouponProductCategoryRelationDao;
import com.macro.mall.dao.SmsCouponProductRelationDao;
import com.macro.mall.dto.SmsCouponParam;
import com.macro.mall.mapper.SmsCouponMapper;
import com.macro.mall.mapper.SmsCouponProductCategoryRelationMapper;
import com.macro.mall.mapper.SmsCouponProductRelationMapper;
import com.macro.mall.model.SmsCouponProductCategoryRelation;
import com.macro.mall.model.SmsCouponProductRelation;
import com.macro.mall.service.impl.SmsCouponServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsCouponServiceImplTest {

    @Mock
    private SmsCouponMapper couponMapper;
    @Mock
    private SmsCouponProductRelationMapper productRelationMapper;
    @Mock
    private SmsCouponProductCategoryRelationMapper productCategoryRelationMapper;
    @Mock
    private SmsCouponProductRelationDao productRelationDao;
    @Mock
    private SmsCouponProductCategoryRelationDao productCategoryRelationDao;
    @Mock
    private SmsCouponDao couponDao;

    private SmsCouponServiceImpl couponService;

    @BeforeEach
    void setUp() {
        couponService = new SmsCouponServiceImpl();
        ReflectionTestUtils.setField(couponService, "couponMapper", couponMapper);
        ReflectionTestUtils.setField(couponService, "productRelationMapper", productRelationMapper);
        ReflectionTestUtils.setField(couponService, "productCategoryRelationMapper", productCategoryRelationMapper);
        ReflectionTestUtils.setField(couponService, "productRelationDao", productRelationDao);
        ReflectionTestUtils.setField(couponService, "productCategoryRelationDao", productCategoryRelationDao);
        ReflectionTestUtils.setField(couponService, "couponDao", couponDao);
    }

    @Test
    void createProductCouponInitializesCountersAndBindsProducts() {
        SmsCouponParam coupon = coupon(2);
        when(couponMapper.insert(coupon)).thenReturn(1);

        int count = couponService.create(coupon);

        assertThat(count).isEqualTo(1);
        assertThat(coupon.getCount()).isEqualTo(coupon.getPublishCount());
        assertThat(coupon.getUseCount()).isZero();
        assertThat(coupon.getReceiveCount()).isZero();
        ArgumentCaptor<List<SmsCouponProductRelation>> captor = ArgumentCaptor.forClass(List.class);
        verify(productRelationDao).insertList(captor.capture());
        assertThat(captor.getValue()).allSatisfy(relation -> assertThat(relation.getCouponId()).isEqualTo(10L));
    }

    @Test
    void createCategoryCouponBindsProductCategories() {
        SmsCouponParam coupon = coupon(1);
        when(couponMapper.insert(coupon)).thenReturn(1);

        int count = couponService.create(coupon);

        assertThat(count).isEqualTo(1);
        ArgumentCaptor<List<SmsCouponProductCategoryRelation>> captor = ArgumentCaptor.forClass(List.class);
        verify(productCategoryRelationDao).insertList(captor.capture());
        assertThat(captor.getValue()).allSatisfy(relation -> assertThat(relation.getCouponId()).isEqualTo(10L));
    }

    @Test
    void deleteRemovesCouponAndBothRelationTypes() {
        when(couponMapper.deleteByPrimaryKey(10L)).thenReturn(1);

        int count = couponService.delete(10L);

        assertThat(count).isEqualTo(1);
        verify(couponMapper).deleteByPrimaryKey(10L);
        verify(productRelationMapper).deleteByExample(any());
        verify(productCategoryRelationMapper).deleteByExample(any());
    }

    private static SmsCouponParam coupon(Integer useType) {
        SmsCouponParam coupon = new SmsCouponParam();
        coupon.setId(10L);
        coupon.setName("coupon");
        coupon.setPublishCount(100);
        coupon.setUseType(useType);
        coupon.setProductRelationList(List.of(new SmsCouponProductRelation()));
        coupon.setProductCategoryRelationList(List.of(new SmsCouponProductCategoryRelation()));
        return coupon;
    }
}
