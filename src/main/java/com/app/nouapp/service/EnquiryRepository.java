package com.app.nouapp.service;

import org.springframework.data.jpa.repository.JpaRepository;


import com.app.nouapp.model.Enquiry;

public interface EnquiryRepository extends JpaRepository<Enquiry, Integer> {

}
