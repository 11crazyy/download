package com.example.httpdownloadserver.dao;

import com.example.httpdownloadserver.dataobject.TaskDO;

import java.util.List;

public interface TaskDAO {
    //增加任务
    int insert(TaskDO taskDO);

    //根据任务id查询任务
    TaskDO selectById(Long id);

    //根据任务id删除任务
    int deleteById(Long id);

    //查询所有任务
    List<TaskDO> selectAll();

    //根据任务状态查询任务
    List<TaskDO> selectByStatus(String status);

}
