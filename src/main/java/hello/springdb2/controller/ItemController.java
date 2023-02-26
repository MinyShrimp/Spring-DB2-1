package hello.springdb2.controller;

import hello.springdb2.domain.Item;
import hello.springdb2.dto.ItemAddDto;
import hello.springdb2.dto.ItemSearchCond;
import hello.springdb2.dto.ItemUpdateDto;
import hello.springdb2.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public String items(
            @ModelAttribute("itemSearch") ItemSearchCond cond,
            Model model
    ) {
        List<Item> items = itemService.findItems(cond);
        model.addAttribute("items", items);
        return "items";
    }

    @GetMapping("/{itemId}")
    public String item(
            @PathVariable long itemId,
            Model model
    ) {
        Item item = itemService.findById(itemId).orElseThrow();
        model.addAttribute("item", item);
        return "item";
    }

    @GetMapping("/add")
    public String addForm() {
        return "addForm";
    }

    @PostMapping("/add")
    public String addItem(
            @ModelAttribute("item") ItemAddDto dto,
            RedirectAttributes redirectAttributes
    ) {
        Item savedItem = itemService.save(dto);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(
            @PathVariable long itemId,
            Model model
    ) {
        Item item = itemService.findById(itemId).orElseThrow();
        model.addAttribute("item", item);
        return "editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String editItem(
            @PathVariable long itemId,
            @ModelAttribute ItemUpdateDto updateParam
    ) {
        itemService.update(itemId, updateParam);
        return "redirect:/items/{itemId}";
    }
}
