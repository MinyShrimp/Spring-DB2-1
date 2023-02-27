package hello.springdb2.example.v2.config;

import hello.springdb2.example.qeurydsl.repository.JpaItemRepositoryV3;
import hello.springdb2.example.v2.repository.ItemQueryRepositoryV2;
import hello.springdb2.example.v2.repository.ItemRepositoryV2;
import hello.springdb2.example.v2.service.ItemServiceV2;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.service.ItemService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class V2Config {
    private final EntityManager em;
    private final ItemRepositoryV2 itemRepositoryV2;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV2(itemRepositoryV2, itemQueryRepository());
    }

    @Bean
    public ItemQueryRepositoryV2 itemQueryRepository() {
        return new ItemQueryRepositoryV2(em);
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV3(em);
    }
}
