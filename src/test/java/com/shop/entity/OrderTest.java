package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderItemRepository;
import com.shop.repository.OrderRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class OrderTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @PersistenceContext
    EntityManager em;

    public Item createItem() {
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("상세설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        return item;
    }

    public Order createOrder(){ // 주문 데이터를 생성해서 저장
        Order order = new Order();

        for (int i=0; i<3;i++){
            Item item = createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        Member member = new Member();
        memberRepository.save(member);

        order.setMember(member);
        orderRepository.save(order);
        return order;
    }



    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest() {

        Order order = new Order();

        for (int i = 0; i < 3; i++) {
            Item item = this.createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem); // 아직 영속성 컨텍스트에 저장되지 않은 orderItem 엔티티를 order 엔티티에 담아준다.
        }

        orderRepository.saveAndFlush(order); // order 엔티티를 저장하면서 강제로 flush를 호출하여 영속성 컨텍스트에 있는 객체들을 DB에 반영한다.
        em.clear(); // 영속성 컨텍스트의 상태를 초기화한다.

        Order savedOrder = orderRepository.findById(order.getId()).orElseThrow(EntityNotFoundException::new); // 영속성 컨텍스트를 초기화했기 때문에 DB에서 주문 엔티티를 조회한다. select 쿼리문이 실행된다.
        assertEquals(3, savedOrder.getOrderItems().size()); // itemOrder 엔티티 3개가 실제로 DB에 저장되었는지 검사한다.
    }
    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest(){
        Order order = this.createOrder();
        order.getOrderItems().remove(0); // order 엔티티에서 관리하고 있는 orderItem 리스트의 0번째 인덱스 요소를 제거
        em.flush();
    }

    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLoadingTest(){
        Order order = this.createOrder(); // 주문 생성 메소드를 이용해 주문 데이터 저장
        Long orderItemId = order.getOrderItems().get(0).getId();
        em.flush();
        em.clear();

        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(EntityNotFoundException::new); // 영속성 컨텍스트의 상태 초기화 후 order 엔티티에 저장햤던 주문 상품 아이디를 이용하여 orderItem을 DB에서 다시 조회
        System.out.println("Order Class : "+ orderItem.getOrder().getClass()); // 1. orderItem 엔티티에 있는 order 객체의 클래스 출력 -> 2. HiberProxy라고 출력 되어 엔티티 객체 대신 프록시 객체 넣음
        // 출력 결과 : Order Class : class com.shop.entity.Order
        // 프록시 객체는 실제로 사용되기 전까지 데이터 로딩을 하지 않고, 실제 사용 시점에 조회 쿼리문이 실행된다.
        System.out.println("===============================");
        orderItem.getOrder().getOrderDate(); // orderDate를 조회할 때 select 쿼리문이 실행된다.
        System.out.println("===============================");
    }


}