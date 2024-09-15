package com.example.httpdownloadserver.dao;

import com.example.httpdownloadserver.dataobject.TaskDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskDAO {
    //增加任务
    int insert(TaskDO taskDO);

    //根据任务id查询任务
    TaskDO selectById(Integer id);

    //根据任务id删除任务
    int deleteById(Integer id);

    //查询所有任务
    List<TaskDO> selectAll();

    //根据任务状态查询任务
    List<TaskDO> selectByStatus(String status);
    //根据所选列表删除文件
    int deleteByIds(List<Integer> ids);
    //根据任务id更新任务
    int updateThreadById(@Param("id") Integer id,@Param("downloadThread") int downloadThread);
    //获得线程数
    int getThreadById(Integer id);
    //根据任务id更新任务
    int updateById(TaskDO taskDO);

}
