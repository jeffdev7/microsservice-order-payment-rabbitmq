package br.com.alurafood.pagamentos.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration

public class Configuration {
    @Bean
    ModelMapper getMapper()
    {
        return new ModelMapper();
    }
}