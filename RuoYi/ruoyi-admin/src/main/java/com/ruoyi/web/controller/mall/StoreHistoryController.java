package com.ruoyi.web.controller.mall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.MemberContextHolder;
import com.ruoyi.system.service.IMallStoreService;

/**
 * 用户端浏览记录（需会员登录）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/store/history")
public class StoreHistoryController extends BaseController
{
    @Autowired
    private IMallStoreService mallStoreService;

    /**
     * 浏览记录列表（按商品去重）
     */
    @GetMapping("/list")
    public TableDataInfo list()
    {
        startPage();
        return getDataTable(mallStoreService.selectHistoryList(MemberContextHolder.requireMemberId()));
    }
}
