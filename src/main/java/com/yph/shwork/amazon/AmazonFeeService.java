package com.yph.shwork.amazon;

import com.oigbuy.common.exception.BusiException;
import com.oigbuy.common.utils.StringUtils;
import com.oigbuy.finance.amazon.dto.AmazonFeeDTO;
import com.oigbuy.finance.common.utils.DateTool;
import com.oigbuy.finance.common.utils.OigDateUtils;
import com.oigbuy.finance.common.utils.ThreadPoolTool;
import com.oigbuy.finance.dao.finance.amazon.AmazonFeeDao;
import com.oigbuy.finance.dao.oms.OmsDao;
import com.oigbuy.finance.dict.DictionaryService;
import com.xxl.job.core.log.XxlJobLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @Author penghai.yin
 * @Date 2021年12月27日14:34:23
 * @Description:
 */
@Service
public class AmazonFeeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private OmsDao omsDao;

    @Autowired
    private AmazonFeeDao amazonFeeDao;

    private Map<String, String> siteMap;
    private Map<String, String> accountMap;

    private static final int SINGLE_COUNT = 10000;
    private static final int BATCH_COUNT = 500;
    private Map<String, String> accountShortNameMap;
    private Map<String, String> currencyMap;

    private Set<String> notFindDicAccountNames;

    public void     process(String updateTimeStart, String updateTimeEnd) {
        /**
         * 退出条件是最后一天日期是当天，而不是数据长度为0
         */
        String tempUpdateTimeStart = updateTimeStart;
        List<AmazonFeeDTO> taskList = null;
        int pullNum = 0;

        boolean flag = true;
        ThreadPoolTool<AmazonFeeDTO> tool = null;
        boolean open = true;//控制线程池创建的开关
//        Date executeTime = new Date();
        try {
            while (flag) {
                if (Long.valueOf(tempUpdateTimeStart) <= Long.valueOf(updateTimeEnd)) {
                    String updateTimeStartParam = OigDateUtils.getSpecifiedDayAfter(tempUpdateTimeStart, 0);
                    String updateTimeEndParam = OigDateUtils.getSpecifiedDayAfter(tempUpdateTimeStart, 1);
                    taskList = omsDao.findAmazonFeeList(updateTimeStartParam, updateTimeEndParam);
                    int size = taskList.size();
                    XxlJobLogger.log("Amazon费用拉取当前处理时间：{}，{}。SQL查询数量：{}", updateTimeStartParam, updateTimeEndParam, size);
                    // 将创建时间+1, 进行下一次的拉取
                    tempUpdateTimeStart = OigDateUtils.getSpecifiedDayAfter(tempUpdateTimeStart, 1, "yyyyMMdd");
                    if (size > 0) {
                        transform(taskList);// 数据转换
                        pullNum += size;
                        if (size > SINGLE_COUNT) {
                            if (open) {
                                tool = new ThreadPoolTool(SINGLE_COUNT);
                                tool.setCallBack((ThreadPoolTool.CallBack<AmazonFeeDTO>) list -> concurrentProcessBatch(list));
                                XxlJobLogger.log("----------初始化线程池--------");
                                open = false;
                            }
                            tool.setList(taskList);
                            CountDownLatch countDownLatch = new CountDownLatch(1);
                            tool.setOuter(countDownLatch);
                            try {
                                tool.execute();
                                countDownLatch.await();
                            } catch (Exception e) {
                                e.printStackTrace();
                                shutdown(tool);
                            } finally {
                                taskList = clear(taskList);
                            }
                        } else {
                            processBatch(taskList);
                        }
                    }
                } else {
                    flag = false;
                    XxlJobLogger.log("Amazon费用拉取执行结束：{}-{}, 已完成。累计拉取：{}", updateTimeStart, updateTimeEnd, pullNum);
                }
            }
        } finally {
            shutdown(tool);
        }
    }

    private List<AmazonFeeDTO> clear(List<AmazonFeeDTO> taskList) {
        if (taskList != null) {
            if (taskList.size() > 0) {
                taskList.clear();
            }
            taskList = null;
        }
        return taskList;
    }

    private void shutdown(ThreadPoolTool<AmazonFeeDTO> tool) {
        if (tool != null) {
            tool.shutdown();
            logger.info("关闭资源");
        }
    }

    /**
     * 单线程情况
     *
     * @param list
     */
    public void processBatch(List<AmazonFeeDTO> list) {
        try {
//            transform(list);
            int total = spiltUpdateBatch(list);// 分批更新
            if (total != list.size()) {
                throw new BusiException("更新结果不符合预期。。。");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XxlJobLogger.log(e.getMessage());
        } finally {
//            list = clear(list);出现ConcurrentModificationException，原因是修改了原来的引用。
        }
    }

    /**
     * 多线程处理
     * @param list
     */
    public void concurrentProcessBatch(List<AmazonFeeDTO> list) {
        try {
//            transform(list);
            int total = concurrentUpdateBatch(list);// 多线程处理
            if (total != list.size()) {
                throw new BusiException("更新结果不符合预期。。。");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XxlJobLogger.log(e.getMessage());
        }
    }

    /**
     * 性能调优：分批处理
     * Mysql连接次数，比如1万条，性能评估：
     * 单个线程处理5千，处理10次
     * 最好的情况：连接10次
     * 最坏的情况：10 + 500*10 = 5010次
     *
     * @param list
     * @return
     * @Author penghai.yin
     */
    private int spiltUpdateBatch(List<AmazonFeeDTO> list) {
        int total = 0;
        if (list.size() > BATCH_COUNT) {
            int time = list.size() % BATCH_COUNT == 0 ? list.size() / BATCH_COUNT : (list.size() / BATCH_COUNT) + 1;
            for (int i = 0; i < time; i++) {
                List<AmazonFeeDTO> amazonFeeDTOS = list.subList(i * BATCH_COUNT, (i + 1) * BATCH_COUNT > list.size() ? list.size() : (i + 1) * BATCH_COUNT);
                int upCnt = amazonFeeDao.updateBatch(amazonFeeDTOS);
                total += upCnt;
                if (upCnt != amazonFeeDTOS.size()) {
                    for (AmazonFeeDTO amazonFeeDTO : amazonFeeDTOS) {
                        int addCnt = amazonFeeDao.insertWhereNotExist(amazonFeeDTO);
                        total += addCnt;
                    }
//                   amazonFeeDTOS = clear(amazonFeeDTOS);
                }
            }
        } else {
            total = amazonFeeDao.updateBatch(list);
            if (total != list.size()) {
                for (AmazonFeeDTO amazonFeeDTO : list) {
                    int addCnt = amazonFeeDao.insertWhereNotExist(amazonFeeDTO);
                    total += addCnt;
                }
            }
        }
        return total;
    }


    private int concurrentUpdateBatch(List<AmazonFeeDTO> list) {
        int total = 0;
        if (list.size() > BATCH_COUNT) {
            int time = list.size() % BATCH_COUNT == 0 ? list.size() /                    BATCH_COUNT : (list.size() / BATCH_COUNT) + 1;
            for (int i = 0; i < time; i++) {
                List<AmazonFeeDTO> amazonFeeDTOS = list.subList(i * BATCH_COUNT, (i + 1) * BATCH_COUNT > list.size() ? list.size() : (i + 1) * BATCH_COUNT);
                int upCnt = amazonFeeDao.updateBatch(amazonFeeDTOS);
                total += upCnt;
                total = insertBatch(total, amazonFeeDTOS, upCnt);
            }
        } else {
            total = amazonFeeDao.updateBatch(list);
            total = insertBatch(total, list, total);
        }
        return total;
    }

    private int insertBatch(int total, List<AmazonFeeDTO> amazonFeeDTOS, int upCnt) {
        if (upCnt != amazonFeeDTOS.size()) {
            List<String> ids = amazonFeeDao.queryIfExist(amazonFeeDTOS);
            List<AmazonFeeDTO> toInsertBatch = amazonFeeDTOS.stream().filter(s -> !ids.contains(s.getOriginId())).collect(Collectors.toList());
            int addCnt = amazonFeeDao.insertBatch(toInsertBatch);
            total += addCnt;
        }
        return total;
    }


    private void transform(List<AmazonFeeDTO> list) {
        for (AmazonFeeDTO amazonFeeDTO : list) {
            amazonFeeDTO.setAccountDic(dictionaryService.getCodeByNameWithMap(accountShortNameMap, amazonFeeDTO.getAccountShortName()));
            amazonFeeDTO.setFeeCurrency(dictionaryService.getCodeByNameWithMap(currencyMap, amazonFeeDTO.getFeeCurrency()));
            amazonFeeDTO.setAccountShortNameFix(accountShortNameMap.get(amazonFeeDTO.getAccountDic()));
            amazonFeeDTO.setFeeTimeBeijing(DateTool.getChinaTimeZoneTime(amazonFeeDTO.getTransTime(), amazonFeeDTO.getFeeTimeZone()));
            /**
             * 这么设计的理由：假如其中拉到的一条数据就是有问题的，但不影响做账什么的。
             */
            if (StringUtils.isEmpty(amazonFeeDTO.getAccountDic()) || StringUtils.isEmpty(amazonFeeDTO.getAccountShortNameFix())) {
                amazonFeeDTO.setStatus(0);
            }
            if(org.apache.commons.lang3.StringUtils.isEmpty(amazonFeeDTO.getAccountDic())){
                notFindDicAccountNames.add(String.format("[accountShortName = %s]", amazonFeeDTO.getAccountShortName()));
            }
        }

    }

    public Map<String, String> getSiteMap() {
        return siteMap;
    }

    public void setSiteMap(Map<String, String> siteMap) {
        this.siteMap = siteMap;
    }

    public Map<String, String> getAccountMap() {
        return accountMap;
    }

    public void setAccountMap(Map<String, String> accountMap) {
        this.accountMap = accountMap;
    }

    public void setAccountShortNameMap(Map<String, String> accountShortNameMap) {
        this.accountShortNameMap = accountShortNameMap;
    }

    public Map<String, String> getAccountShortNameMap() {
        return accountShortNameMap;
    }

    public Map<String, String> getCurrencyMap() {
        return currencyMap;
    }

    public void setCurrencyMap(Map<String, String> currencyMap) {
        this.currencyMap = currencyMap;
    }

    public void setNotFindDicAccountNames(Set<String> notFindDicAccountNames) {
        this.notFindDicAccountNames = notFindDicAccountNames;
    }
}
