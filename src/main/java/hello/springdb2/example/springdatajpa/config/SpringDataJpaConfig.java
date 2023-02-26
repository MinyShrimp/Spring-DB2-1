package hello.springdb2.example.springdatajpa.config;

import hello.springdb2.example.springdatajpa.repository.JpaItemRepositoryV2;
import hello.springdb2.example.springdatajpa.repository.SpringDataJpaItemRepository;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.service.ItemService;
import hello.springdb2.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SpringDataJpaConfig {

    private final SpringDataJpaItemRepository springDataJpaItemRepository;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV2(springDataJpaItemRepository);
    }
}
