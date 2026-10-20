package com.example.firstspringbootapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.firstspringbootapp.entity.ItemEntity;

public interface ItemRepository extends JpaRepository<ItemEntity, Integer> {
}


