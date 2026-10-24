package com.ruoyi.web.controller.mall;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.MemberContextHolder;
import com.ruoyi.system.domain.vo.StoreFavoriteVO;
import com.ruoyi.system.service.IMallStoreService;

/**
 * 用户端收藏（需会员登录）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/store/favorites")
public class StoreFavoriteController extends BaseController
{
    @Autowired
    private IMallStoreService mallStoreService;

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/{goodsId}")
    public AjaxResult toggle(@PathVariable Long goodsId)
    {
        boolean favored = mallStoreService.toggleFavorite(MemberContextHolder.requireMemberId(), goodsId);
        Map<String, Object> data = new HashMap<>();
        data.put("isFavorite", favored);
        return success(data);
    }

    /**
     * 收藏列表
     */
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<StoreFavoriteVO> list = mallStoreService.selectFavoriteList(MemberContextHolder.requireMemberId());
        return success(list);
    }

    /**
     * 是否已收藏
     */
    @GetMapping("/{goodsId}/check")
    public AjaxResult check(@PathVariable Long goodsId)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("isFavorite", mallStoreService.isFavorite(MemberContextHolder.requireMemberId(), goodsId));
        return success(data);
    }
}
