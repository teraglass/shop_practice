package com.shop.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 하나의 Cart에는 여러 상품을 담을 수 있으므로 다대일 매핑
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id") // 장바구니에 담을 상품의 정보를 알아야 하므로 상품 엔티티 매핑
    private Item item;

    private int count; // 같은 상품을 장바구니에 몇 개 담을 지 저장
}
