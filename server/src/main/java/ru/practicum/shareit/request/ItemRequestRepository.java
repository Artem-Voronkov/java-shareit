package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("select r from ItemRequest r where r.requestor.id = :userId order by r.created desc")
    List<ItemRequest> findOwn(@Param("userId") Long userId);

    @Query("select r from ItemRequest r where r.requestor.id <> :userId order by r.created desc")
    List<ItemRequest> findOthers(@Param("userId") Long userId);
}