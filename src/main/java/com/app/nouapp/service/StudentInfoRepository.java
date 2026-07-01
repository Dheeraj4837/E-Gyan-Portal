package com.app.nouapp.service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.nouapp.model.StudentInfo;

public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {

}
