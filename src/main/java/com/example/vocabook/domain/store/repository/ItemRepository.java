package com.example.vocabook.domain.store.repository;

import com.example.vocabook.domain.store.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
