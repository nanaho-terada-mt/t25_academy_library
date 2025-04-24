package jp.co.metateam.library.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jakarta.el.ELException;
import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;

/**
 * 書籍関連クラス
 */
@Log4j2
@Controller
public class BookController {
    
    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        
        model.addAttribute("bookMstList", bookMstList);

        return "book/index";
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }
        return "book/add";
    }

    @PostMapping("/book/add")
    public String register(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra) {
        
        //書籍名チェック
        if(StringUtils.isEmpty(bookMstDto.getTitle())){
            result.rejectValue("title", "required", "書籍名は必須です");
        } else {if(bookMstDto.getTitle().length() > 255){
                result.rejectValue("title", "length", "書籍名は255字以内で入力してください");
            } 
        }
        
        //ISBNチェック
        if(StringUtils.isEmpty(bookMstDto.getTitle())) {
            result.rejectValue("isbn", "required", "ISBNは必須です");
            }
        if(bookMstDto.getIsbn().length() != 13){
                result.rejectValue("isbn", "length", "ISBNは13字で入力してください");
            }
        if (!bookMstDto.getIsbn().matches("\\d+")) {
            result.rejectValue("isbn", "format", "ISBNは半角数字で入力してください");
        } 

        if (result.hasErrors()){
            return "book/add";
        }
           
        if (bookMstService.isbnExists(bookMstDto.getIsbn()) > 0){
            result.rejectValue("isbn", "duplicate", "このISBNは既に登録されています");
        }
    
        if (result.hasErrors()){
            return "book/add";
        }
        
        //書籍を登録する
        bookMstService.save(bookMstDto);
            
        return "redirect:/book/index";
        
    }
} 


