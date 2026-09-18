package com.macro.mall.service;

import com.macro.mall.dao.OmsOrderDao;
import com.macro.mall.dao.OmsOrderOperateHistoryDao;
import com.macro.mall.dto.OmsOrderDeliveryParam;
import com.macro.mall.dto.OmsReceiverInfoParam;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.mapper.OmsOrderOperateHistoryMapper;
import com.macro.mall.model.OmsOrderOperateHistory;
import com.macro.mall.service.impl.OmsOrderServiceImpl;
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
class OmsOrderServiceImplTest {

    @Mock
    private OmsOrderMapper orderMapper;
    @Mock
    private OmsOrderDao orderDao;
    @Mock
    private OmsOrderOperateHistoryDao orderOperateHistoryDao;
    @Mock
    private OmsOrderOperateHistoryMapper orderOperateHistoryMapper;

    private OmsOrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OmsOrderServiceImpl();
        ReflectionTestUtils.setField(orderService, "orderMapper", orderMapper);
        ReflectionTestUtils.setField(orderService, "orderDao", orderDao);
        ReflectionTestUtils.setField(orderService, "orderOperateHistoryDao", orderOperateHistoryDao);
        ReflectionTestUtils.setField(orderService, "orderOperateHistoryMapper", orderOperateHistoryMapper);
    }

    @Test
    void deliveryUpdatesOrdersAndWritesDeliveryHistory() {
        when(orderDao.delivery(anyList())).thenReturn(2);

        int count = orderService.delivery(List.of(deliveryParam(1L), deliveryParam(2L)));

        assertThat(count).isEqualTo(2);
        ArgumentCaptor<List<OmsOrderOperateHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderOperateHistoryDao).insertList(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue()).extracting(OmsOrderOperateHistory::getOrderId).containsExactly(1L, 2L);
        assertThat(captor.getValue()).allSatisfy(history -> assertThat(history.getOrderStatus()).isEqualTo(2));
    }

    @Test
    void closeMarksOrdersClosedAndWritesHistory() {
        when(orderMapper.updateByExampleSelective(any(), any())).thenReturn(2);

        int count = orderService.close(List.of(1L, 2L), "buyer request");

        assertThat(count).isEqualTo(2);
        ArgumentCaptor<List<OmsOrderOperateHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderOperateHistoryDao).insertList(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue()).allSatisfy(history -> {
            assertThat(history.getOrderStatus()).isEqualTo(4);
            assertThat(history.getNote()).contains("buyer request");
        });
    }

    @Test
    void updateReceiverInfoUpdatesOrderAndWritesSingleHistory() {
        when(orderMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

        int count = orderService.updateReceiverInfo(receiverInfoParam());

        assertThat(count).isEqualTo(1);
        verify(orderMapper).updateByPrimaryKeySelective(any());
        ArgumentCaptor<OmsOrderOperateHistory> captor = ArgumentCaptor.forClass(OmsOrderOperateHistory.class);
        verify(orderOperateHistoryMapper).insert(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(1L);
        assertThat(captor.getValue().getOrderStatus()).isEqualTo(1);
    }

    private static OmsOrderDeliveryParam deliveryParam(Long orderId) {
        OmsOrderDeliveryParam param = new OmsOrderDeliveryParam();
        param.setOrderId(orderId);
        param.setDeliveryCompany("SF");
        param.setDeliverySn("SF" + orderId);
        return param;
    }

    private static OmsReceiverInfoParam receiverInfoParam() {
        OmsReceiverInfoParam param = new OmsReceiverInfoParam();
        param.setOrderId(1L);
        param.setStatus(1);
        param.setReceiverName("receiver");
        param.setReceiverPhone("13800000000");
        param.setReceiverPostCode("100000");
        param.setReceiverProvince("province");
        param.setReceiverCity("city");
        param.setReceiverRegion("region");
        param.setReceiverDetailAddress("address");
        return param;
    }
}
