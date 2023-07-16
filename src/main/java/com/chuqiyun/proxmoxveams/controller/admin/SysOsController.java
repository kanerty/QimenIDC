package com.chuqiyun.proxmoxveams.controller.admin;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.chuqiyun.proxmoxveams.annotation.AdminApiCheck;
import com.chuqiyun.proxmoxveams.annotation.AdminLoginCheck;
import com.chuqiyun.proxmoxveams.annotation.PublicSysApiCheck;
import com.chuqiyun.proxmoxveams.entity.Master;
import com.chuqiyun.proxmoxveams.entity.Os;
import com.chuqiyun.proxmoxveams.service.MasterService;
import com.chuqiyun.proxmoxveams.service.OsService;
import com.chuqiyun.proxmoxveams.utils.ClientApiUtil;
import com.chuqiyun.proxmoxveams.utils.ModUtil;
import com.chuqiyun.proxmoxveams.utils.ResponseResult;
import com.chuqiyun.proxmoxveams.utils.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @author mryunqi
 * @date 2023/7/9
 */
@Slf4j
@RestController
public class SysOsController {
    @Value("${config.admin_path}")
    private String ADMIN_PATH;

    @Value("${config.secret}")
    private String secret;
    @Value("${config.os_url}")
    private String osUrl;
    @Resource
    private OsService osService;
    @Resource
    private MasterService masterService;
    @AdminApiCheck
    @GetMapping("/{adminPath}/selectOsByOnline")
    public ResponseResult<JSONObject> selectOsByOnline(@PathVariable("adminPath") String adminPath,
                                           @RequestParam(name = "page",defaultValue = "1") Integer page,
                                           @RequestParam(name = "size", defaultValue = "20") Integer size) throws UnauthorizedException {
        if (!adminPath.equals(ADMIN_PATH)){
            //判断后台路径是否正确
            return ResponseResult.fail(ResponseResult.RespCode.NOT_PERMISSION);
        }
        JSONObject osOnlineResult = ClientApiUtil.getNetOs(osUrl);
        // 分页，获取分页数据
        osOnlineResult = ModUtil.jsonObjectPage(osOnlineResult,page,size);
        JSONArray jsonArray = osOnlineResult.getJSONArray("data");
        for (int i = 0; i < jsonArray.size(); i++){
            // 将其转换为json对象
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            // 获取第一个key
            String key = jsonObject.keySet().iterator().next();
            JSONObject value = jsonObject.getJSONObject(key);
            JSONArray nodeData = selectOsByNameNodes(key);
            value.put("nodeData",nodeData);
            jsonObject.put(key, value);
            // 更新jsonArray
            jsonArray.set(i,jsonObject);
        }
        osOnlineResult.put("data",jsonArray);
        return ResponseResult.ok(osOnlineResult);

    }

    public JSONArray selectOsByNameNodes(String osName){
        // 获取所有节点id
        List<Integer> nodeIdList = masterService.getAllNodeIdList();
        JSONArray jsonArray = new JSONArray();
        for (Integer nodeId : nodeIdList){
            Os os = osService.selectOsByNameAndNodeId(osName, nodeId);
            if (os!=null){
                // 已安装
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("nodeId",nodeId);
                jsonObject.put("status",os.getStatus());
                jsonObject.put("osId",os.getId());
                jsonObject.put("size",os.getSize());
                jsonObject.put("path",os.getPath());
                // 判断是否正在下载
                if (os.getStatus()==1){
                    jsonObject.put("schedule",os.getSchedule());
                }
                jsonObject.put("createTime",os.getCreateTime());
                jsonArray.add(jsonObject);
            }
        }
        return jsonArray;
    }
}
