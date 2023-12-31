package com.fastcampus.batchcampus.application.dormant;

import com.fastcampus.batchcampus.batch.ItemReader;
import com.fastcampus.batchcampus.customer.Customer;
import com.fastcampus.batchcampus.customer.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class AllCustomerItemRedaer implements ItemReader<Customer> {
    private final CustomerRepository customerRepository;
    private int pageNo = 0;

    public AllCustomerItemRedaer(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer read() {
        final PageRequest pageRequest = PageRequest.of(pageNo, 1, Sort.by("id").ascending());
        final Page<Customer> page = customerRepository.findAll(pageRequest);

        final Customer customer;
        if (page.isEmpty()) {
            pageNo = 0;
            return null;
        } else {
            pageNo++;
            return customer = page.getContent().get(0);
        }
    }
}
