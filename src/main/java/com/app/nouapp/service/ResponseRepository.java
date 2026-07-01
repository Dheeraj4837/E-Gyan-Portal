package com.app.nouapp.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.nouapp.model.Response;

public interface ResponseRepository extends JpaRepository<Response, Integer>{

	@Query("select r from Response r where r.responsetype = :responsetype")
	List<Response> findByResponseType(@Param("responsetype") String string);

}
