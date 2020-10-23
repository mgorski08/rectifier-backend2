package com.example.rectifierBackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

@Configuration
@EnableTransactionManagement
public class TransactionConfig implements TransactionManagementConfigurer {

    @Autowired
    private PlatformTransactionManager myTxManager;

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return this.myTxManager;
    }
}