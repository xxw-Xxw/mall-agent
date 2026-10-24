package com.ruoyi.web.controller.mall;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.MemberContextHolder;
import com.ruoyi.system.domain.vo.StoreCartVO;
import com.ruoyi.system.service.IMallStoreService;

/**
 * 用户端购物车（需会员登录）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/store/cart")
public class StoreCartController extends BaseController
{
    @Autowired
    private IMallStoreService mallStoreService;

    /**
     * 加入购物车
     */
    @PostMapping
    public AjaxResult add(Long goodsId, Long quantity)
    {
        mallStoreService.addCart(MemberContextHolder.requireMemberId(), goodsId, quantity);
        return success();
    }

    /**
     * 购物车列表
     */
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<StoreCartVO> list = mallStoreService.selectCartList(MemberContextHolder.requireMemberId());
        return success(list);
    }

    /**
     * 修改数量
     */
    @PutMapping("/{cartId}")
    public AjaxResult update(@PathVariable Long cartId, Long quantity)
    {
        mallStoreService.updateCartQuantity(MemberContextHolder.requireMemberId(), cartId, quantity);
        return success();
    }

    /**
     * 删除购物车项（支持批量）
     */
    @DeleteMapping("/{cartIds}")
    public AjaxResult remove(@PathVariable Long[] cartIds)
    {
        return toAjax(mallStoreService.deleteCarts(MemberContextHolder.requireMemberId(), cartIds));
    }
}
