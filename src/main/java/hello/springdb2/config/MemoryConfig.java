package hello.springdb2.config;

import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.memory.MemoryItemRepository;
import hello.springdb2.service.ItemService;
import hello.springdb2.service.ItemServiceV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryConfig {
    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new MemoryItemRepository();
    }
}
