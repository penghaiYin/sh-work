package com.yph.shwork.fuiou.service;

import cn.hutool.core.util.IdcardUtil;
import com.alibaba.fastjson.JSON;
import com.oigbuy.common.dto.dingding.OigMarkdownMessageDTO;
import com.oigbuy.common.feign.auth.token.TokenFeignService;
import com.oigbuy.common.feign.dingding.DingdingFeignService;
import com.oigbuy.common.http.JsonResult;
import com.oigbuy.common.pojo.check.export.constant.FileManageConstant;
import com.oigbuy.common.pojo.check.orderwithdrawal.dto.OrderWithdrawalReqDTO;
import com.oigbuy.common.pojo.check.orderwithdrawal.dto.WithdrawalSettlementParamReqDTO;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.*;
import com.oigbuy.finance.common.utils.DateTool;
import com.oigbuy.finance.dao.finance.orderwithdrawal.*;
import com.oigbuy.finance.fuiou.entity.withdrawalorder.OrderWithdrawalDetail;
import com.oigbuy.finance.fuiou.utils.MchntIdGeneratorUtils;
import com.oigbuy.finance.odoo.service.BuildOdooParamService;
import com.oigbuy.finance.odoo.service.OdooService;
import com.oigbuy.finance.orderwithdrawal.service.FyOrderDetailService;
import com.oigbuy.finance.orderwithdrawal.service.FyOrderService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SrmWithdrawalService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private FyOrderService fyOrderService;
	
	@Autowired
	private FyOrderDetailService fyOrderDetailService;
	
	@Autowired
	private SrmWithdrawalBatchDao srmWithdrawalBatchDao;
	
	@Autowired
	private SrmWithdrawalMainDao srmWithdrawalMainDao;
	
	@Autowired
	private SrmWithdrawalDetailDao srmWithdrawalDetailDao;
	
	@Autowired
	private SrmWithdrawalDetailSplitDao srmWithdrawalDetailSplitDao;
	
	@Autowired
	private OdooService odooService;
	
	@Autowired
	PlatformTransactionManager transactionManager;
	
	@Value("${fy.collection-order.txnTp}")
	private String txnTp;
	
	@Value("${fy.collection-order.settleAccountsTp}")
	private String settleAccountsTp;
	
	@Value("${fy.collection-order.outAcntNoCHT}")
	private String outAcntNoCHT;
	
	@Value("${fy.collection-order.outAcntNmCHT}")
	private String outAcntNmCHT;
	
	@Value("${fy.collection-order.outAcntBankNmCHT}")
	private String outAcntBankNmCHT;
	
	@Value("${fy.collection-order.countryCdCHT}")
	private String countryCdCHT;
	
	@Value("${fy.collection-order.bankCountryCdCHT}")
	private String bankCountryCdCHT;
	
	@Value("${fy.collection-order.outCurCdCHT}")
	private String outCurCdCHT;
	
	@Value("${fy.collection-order.curCdCHT}")
	private String curCdCHT;

	@Value("${fy.collection-order.outAcntNoHC}")
	private String outAcntNoHC;

	@Value("${fy.collection-order.outAcntNmHC}")
	private String outAcntNmHC;

	@Value("${fy.collection-order.outAcntBankNmHC}")
	private String outAcntBankNmHC;

	@Value("${fy.collection-order.countryCdHC}")
	private String countryCdHC;

	@Value("${fy.collection-order.bankCountryCdHC}")
	private String bankCountryCdHC;

	@Value("${fy.collection-order.outCurCdHC}")
	private String outCurCdHC;

	@Value("${fy.collection-order.curCdHC}")
	private String curCdHC;
	
	@Value("${fy.collection-order.orderType}")
	private String orderType;
	
	@Value("${fy.collection-order.orderTp}")
	private String orderTp;
	
	@Value("${fy.collection-order.ver}")
	private String ver;
	
	@Value("${auth.username}")
	private String authUsername;
	
	@Value("${auth.password}")
	private String authPassword;
	
	@Value("${dingding.fy-warning.client-id}")
	private String fyWarningClientId;
	
	@Autowired
	private TokenFeignService tokenFeignService;
		
	@Autowired
	private DingdingFeignService dingdingFeignService;
	
	@Autowired
	private BuildOdooParamService buildOdooParamService;

	@Autowired
	private OrderWithdrawalDao orderWithdrawalDao;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	public void getReceiptInfoFromOdoo() throws Exception {
		List<SrmWithdrawalMain> srmWithdrawalList = srmWithdrawalMainDao.findListToProcess("0");
		if(!CollectionUtils.isEmpty(srmWithdrawalList)){
			for(SrmWithdrawalMain srmWithdrawalMain : srmWithdrawalList) {
				String dataStatus = "1";
				/**
				 * 从ODOO获取收款人信息
				 */
				String account_name = "";
				String payee_idnum = "";
				String phone = "";
				try {
					Map<String, String> odooMap = odooService.getReceiptInfo(buildOdooParamService.buildOdooReceiptInfoParam(srmWithdrawalMain.getSupplierId()));
					account_name = odooMap.get("account_name");
					payee_idnum = odooMap.get("payee_idnum");
					phone = odooMap.get("phone");
					// 非空校验
					if(StringUtils.isBlank(account_name)
							|| StringUtils.isBlank(payee_idnum)
							|| StringUtils.isBlank(phone)){
						// 钉钉预警
						sendDingding(srmWithdrawalMain.getSupplierId()+"", "从odoo获取的字段存在空值",0);
						dataStatus = "3";
					}else {
						// 身份证校验
						boolean valid = IdcardUtil.isValidCard(payee_idnum);
						if(!valid){
							// 钉钉预警
							sendDingding(srmWithdrawalMain.getSupplierId()+"", "身份证校验",0);
							dataStatus = "3";
						}
					}
				} catch (Exception e) {
					sendDingding(srmWithdrawalMain.getSupplierId()+"", "调用odoo无法获取数据",0);
					dataStatus = "3";
				}
				// 获取需要提现的明细订单列表
				List<SrmWithdrawalDetail> srmWithdrawalDetailList = srmWithdrawalDetailDao.findListByWithdrawalCode(srmWithdrawalMain.getWithdrawalCode());
				for(SrmWithdrawalDetail srmWithdrawalDetail : srmWithdrawalDetailList) {
					srmWithdrawalDetail.setReceiptName(account_name);
					srmWithdrawalDetail.setReceiptIdcardNo(payee_idnum);
					srmWithdrawalDetail.setReceiptTelNumber(phone);
					srmWithdrawalDetailDao.updateReceiptInfoById(srmWithdrawalDetail);
				}

				srmWithdrawalMain.setDataStatus(dataStatus);
				srmWithdrawalMainDao.updateStatusOrMchntOrderIdByWithdrawalCode(srmWithdrawalMain);
			}
		}

		/**
		 * 生成明细数据
		 */
		//进行数据拆分
		List<SrmWithdrawalDetailSplit> srmWithdrawalDetailSplitList = new ArrayList<SrmWithdrawalDetailSplit>();
		List<SrmWithdrawalMain> srmWithdrawalMainList = srmWithdrawalMainDao.findListToProcess("1");
		if(CollectionUtils.isEmpty(srmWithdrawalMainList)){
			return;
		}
		for(SrmWithdrawalMain srmWithdrawalMain : srmWithdrawalMainList) {
			srmWithdrawalMain.setDataStatus("2");
			// 获取需要提现的明细订单列表
			List<SrmWithdrawalDetail> srmWithdrawalDetailList = srmWithdrawalDetailDao.findListByWithdrawalCode(srmWithdrawalMain.getWithdrawalCode());
			for(SrmWithdrawalDetail srmWithdrawalDetail : srmWithdrawalDetailList) {
				// 拆分子订单（1:采购成本 2:推广费）
				List<SrmWithdrawalDetailSplit> tmpSplitSrmWithdrawalDetailList = splitSrmWithdrawalDetail(srmWithdrawalMain.getPoOrderCode(), srmWithdrawalDetail);
				srmWithdrawalDetailSplitList.addAll(tmpSplitSrmWithdrawalDetailList);
			}
		}
		/**
		 * 更新相关表信息,进行事务控制
		 */
		TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionAttribute());
		try {
			// 更新t_srm_withdrawal_main
			for(SrmWithdrawalMain srmWithdrawalMain : srmWithdrawalMainList) {
				srmWithdrawalMainDao.updateStatusOrMchntOrderIdByWithdrawalCode(srmWithdrawalMain);
			}

			// 插入t_srm_withdrawal_detail_split
			if(srmWithdrawalDetailSplitList.size() > 0) {
				srmWithdrawalDetailSplitDao.insertList(srmWithdrawalDetailSplitList);
			}
			transactionManager.commit(transaction);
		} catch (Exception e) {
			transactionManager.rollback(transaction);
			throw new Exception("操作失败！"  + ExceptionUtils.getMessage(e));
		}finally{
			if(transaction!=null && !transaction.isCompleted()){
				transactionManager.rollback(transaction);
			}
		}
	}
	
	public void process() throws Exception {

		SrmWithdrawalBatch srmWithdrawalBatch = new SrmWithdrawalBatch();
		List<SrmWithdrawalDetail> allSrmWithdrawalDetailList = new ArrayList<SrmWithdrawalDetail>();
		List<SrmWithdrawalDetailSplit> srmWithdrawalDetailSplitList = new ArrayList<SrmWithdrawalDetailSplit>();
		FyOrder fyOrder  = new FyOrder();
		List<FyOrderDetail> fyOrderDetailList = new ArrayList<FyOrderDetail>();
		BigDecimal totalAmount = new BigDecimal(0);

		// 生成一个商户订单号
		String mchntOrderId = MchntIdGeneratorUtils.getMchntOrderId();
		srmWithdrawalBatch.setMchntOrderId(mchntOrderId);
		srmWithdrawalBatch.setProcessStatus(1);

		/**
		 * 生成富友收汇单请求参数
		 */
		fyOrder.setOrderId(mchntOrderId);
		fyOrder.setTxnTp(txnTp);
		fyOrder.setSettleAccountsTp(settleAccountsTp);
//		fyOrder.setOutAcntNo(outAcntNo);
//		fyOrder.setOutAcntNm(outAcntNm);
//		fyOrder.setOutAcntBankNm(outAcntBankNm);
//		fyOrder.setCountryCd(countryCd);
//		fyOrder.setBankCountryCd(bankCountryCd);
//		fyOrder.setOutCurCd(outCurCd);
		fyOrder.setOrderTp(orderTp);
		fyOrder.setOrderType(orderType);
		fyOrder.setVer(ver);

		/**
		 * 生成明细数据
		 */
		List<SrmWithdrawalMain> srmWithdrawalList = srmWithdrawalMainDao.findListToProcess("1");
		for(SrmWithdrawalMain srmWithdrawalMain : srmWithdrawalList) {

			srmWithdrawalMain.setMchntOrderId(mchntOrderId);
			srmWithdrawalMain.setDataStatus("2");

			totalAmount = totalAmount.add(srmWithdrawalMain.getWithdrawalTotalAmount());

			// 获取需要提现的明细订单列表
			List<SrmWithdrawalDetail> srmWithdrawalDetailList = srmWithdrawalDetailDao.findListByWithdrawalCode(srmWithdrawalMain.getWithdrawalCode());
			for(SrmWithdrawalDetail srmWithdrawalDetail : srmWithdrawalDetailList) {

				// 拆分子订单（1:采购成本 2:推广费）
				List<SrmWithdrawalDetailSplit> tmpSplitSrmWithdrawalDetailList = splitSrmWithdrawalDetail(srmWithdrawalMain.getPoOrderCode(), srmWithdrawalDetail);
				srmWithdrawalDetailSplitList.addAll(tmpSplitSrmWithdrawalDetailList);

				for(SrmWithdrawalDetailSplit srmWithdrawalDetailSplit : tmpSplitSrmWithdrawalDetailList) {
					/**
					 * 生成富友收汇单明细文件参数
					 */
					FyOrderDetail fyOrderDetail = new FyOrderDetail();
					fyOrderDetail.setMchntOrderId(srmWithdrawalMain.getMchntOrderId());
					fyOrderDetail.setMchntOrderNo(srmWithdrawalDetailSplit.getMchntDetailOrderId());
					fyOrderDetail.setOrderDate(sdf.format(srmWithdrawalMain.getWithdrawalTime()));
					fyOrderDetail.setCustNm(srmWithdrawalDetail.getReceiptName());
					fyOrderDetail.setCustId(srmWithdrawalDetail.getReceiptIdcardNo());
					fyOrderDetail.setCustContactNumber(srmWithdrawalDetail.getReceiptTelNumber());
					fyOrderDetail.setOrderAmt(srmWithdrawalDetailSplit.getSplitAmount().multiply(new BigDecimal(100)).intValue());
//					fyOrderDetail.setCurCd(curCd);
//					fyOrderDetail.setCountryCd(countryCd);
//					fyOrderDetail.setOutAcntNo(outAcntNo);
//					fyOrderDetail.setOutAcntNm(outAcntNm);
					fyOrderDetail.setOrderType(orderType);
					fyOrderDetail.setGoodsNm(srmWithdrawalDetail.getProductName());
					fyOrderDetail.setGoodsNo(srmWithdrawalDetailSplit.getSplitQty());
					fyOrderDetail.setGoodsPrice(srmWithdrawalDetailSplit.getSplitPriceUnit().multiply(new BigDecimal(100)).intValue());
					fyOrderDetail.setGoodsCatagory(srmWithdrawalDetail.getGoodsCategory());
					fyOrderDetailList.add(fyOrderDetail);
				}
			}
			allSrmWithdrawalDetailList.addAll(srmWithdrawalDetailList);

		}

		srmWithdrawalBatch.setTotalAmount(totalAmount);
		fyOrder.setOrderAmt(totalAmount.multiply(new BigDecimal(100)).intValue());

		/**
		 * 进行数据的保存和更新，进行事务的控制
		 */
		TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionAttribute());
		try {
			// 插入t_srm_withdrawal_batch
			srmWithdrawalBatchDao.insert(srmWithdrawalBatch);

			// 更新t_srm_withdrawal_main
			for(SrmWithdrawalMain srmWithdrawalMain : srmWithdrawalList) {
				srmWithdrawalMainDao.updateStatusOrMchntOrderIdByWithdrawalCode(srmWithdrawalMain);
			}

			// 插入t_srm_withdrawal_detail_split
			if(srmWithdrawalDetailSplitList.size() > 0) {
				srmWithdrawalDetailSplitDao.insertList(srmWithdrawalDetailSplitList);
			}

			// 插入t_fy_order
			fyOrderService.insert(fyOrder);

			// 插入t_fy_order_detail
			if(fyOrderDetailList.size() > 0) {
				fyOrderDetailService.insetList(fyOrderDetailList);
			}

			transactionManager.commit(transaction);
		} catch (Exception e) {
			transactionManager.rollback(transaction);
			throw new Exception("操作失败！"  + ExceptionUtils.getMessage(e));
		}finally{
			if(transaction!=null && !transaction.isCompleted()){
				transactionManager.rollback(transaction);
			}
		}

	}

	public void settlementExchangeProcess(WithdrawalSettlementParamReqDTO dto) throws Exception{
		List<OrderWithdrawalDetail> list = null;
		OrderWithdrawalReqDTO orderWithdrawalReqDTO = null;
		String fyAccountCode = null;
		//勾选导出
		if (FileManageConstant.CHECK_EXPORT.equals(dto.getExportType())) {
			List<String> mchntDetailOrderIdList = dto.getMchntDetailOrderIdList();
			list = orderWithdrawalDao.findExportWithCheck(mchntDetailOrderIdList);
			fyAccountCode = dto.getFyAccountCode();
			for(OrderWithdrawalDetail orderWithdrawalDetail : list){
				if(!"9".equals(orderWithdrawalDetail.getDocumentStatus())){
					throw new RuntimeException("数据已更新,请刷新后重新处理!");
				}
			}
		} else {//全量导出
			orderWithdrawalReqDTO = dto.getQueryParam();
			fyAccountCode = dto.getQueryParam().getFyAccountCode();
			try {
				orderWithdrawalReqDTO.setDateStartDate(DateTool.getStartDateTime(orderWithdrawalReqDTO.getDateStart()));
				orderWithdrawalReqDTO.setDateEndDate(DateTool.getEndDateTime(orderWithdrawalReqDTO.getDateEnd()));
			} catch (ParseException e) {
				logger.error("时间转换错误：入参：{}", JSON.toJSONString(dto), e);
				throw new RuntimeException("时间转换错误");
			}
			list = orderWithdrawalDao.findListByPage(orderWithdrawalReqDTO);
		}
		if (CollectionUtils.isEmpty(list)) {
			throw new RuntimeException("未查询到提现数据!");
		}
		SrmWithdrawalBatch srmWithdrawalBatch = new SrmWithdrawalBatch();
		FyOrder fyOrder  = new FyOrder();
		List<FyOrderDetail> fyOrderDetailList = new ArrayList<FyOrderDetail>();
		BigDecimal totalAmount = new BigDecimal(0);
		// 生成一个商户订单号
		String mchntOrderId = MchntIdGeneratorUtils.getMchntOrderId();
		srmWithdrawalBatch.setMchntOrderId(mchntOrderId);
		srmWithdrawalBatch.setProcessStatus(1);
		srmWithdrawalBatch.setFyAccountCode(fyAccountCode);
		/**
		 * 生成富友收汇单请求参数
		 */
		fyOrder.setOrderId(mchntOrderId);
		fyOrder.setFyAccountCode(fyAccountCode);
		fyOrder.setTxnTp(txnTp);
		fyOrder.setSettleAccountsTp(settleAccountsTp);
		if("ACC001".equals(fyAccountCode)){
			fyOrder.setOutAcntNo(outAcntNoHC);
			fyOrder.setOutAcntNm(outAcntNmHC);
			fyOrder.setOutAcntBankNm(outAcntBankNmHC);
			fyOrder.setCountryCd(countryCdHC);
			fyOrder.setBankCountryCd(bankCountryCdHC);
			fyOrder.setOutCurCd(outCurCdHC);
		}else{
			fyOrder.setOutAcntNo(outAcntNoCHT);
			fyOrder.setOutAcntNm(outAcntNmCHT);
			fyOrder.setOutAcntBankNm(outAcntBankNmCHT);
			fyOrder.setCountryCd(countryCdCHT);
			fyOrder.setBankCountryCd(bankCountryCdCHT);
			fyOrder.setOutCurCd(outCurCdCHT);
		}
		fyOrder.setOrderTp(orderTp);
		fyOrder.setOrderType(orderType);
		fyOrder.setVer(ver);

		/**
		 * 生成明细数据
		 */
		HashSet<String> hashSet = new HashSet<>();
		for(OrderWithdrawalDetail orderWithdrawalDetail : list){
			totalAmount = totalAmount.add(orderWithdrawalDetail.getTransactionAmount());
			hashSet.add(orderWithdrawalDetail.getMchntDetailOrderId());
			FyOrderDetail fyOrderDetail = new FyOrderDetail();
			fyOrderDetail.setMchntOrderId(mchntOrderId);
			fyOrderDetail.setMchntOrderNo(orderWithdrawalDetail.getMchntDetailOrderId());
			fyOrderDetail.setOrderDate(sdf.format(orderWithdrawalDetail.getWithdrawalTime()));
			fyOrderDetail.setCustNm(orderWithdrawalDetail.getReceiptName());
			fyOrderDetail.setCustId(orderWithdrawalDetail.getReceiptIdCardNo());
			fyOrderDetail.setCustContactNumber(orderWithdrawalDetail.getReceiptTelNumber());
			fyOrderDetail.setOrderAmt(orderWithdrawalDetail.getTransactionAmount().multiply(new BigDecimal(100)).intValue());
			if("ACC001".equals(orderWithdrawalDetail.getFyAccountCode())){
				fyOrderDetail.setCurCd(curCdHC);
				fyOrderDetail.setCountryCd(countryCdHC);
				fyOrderDetail.setOutAcntNo(outAcntNoHC);
				fyOrderDetail.setOutAcntNm(outAcntNmHC);
			}else{
				fyOrderDetail.setCurCd(curCdCHT);
				fyOrderDetail.setCountryCd(countryCdCHT);
				fyOrderDetail.setOutAcntNo(outAcntNoCHT);
				fyOrderDetail.setOutAcntNm(outAcntNmCHT);
			}
			fyOrderDetail.setOrderType(orderType);
			fyOrderDetail.setGoodsNm(orderWithdrawalDetail.getProductName());
			fyOrderDetail.setGoodsNo(orderWithdrawalDetail.getQty());
			fyOrderDetail.setGoodsPrice(orderWithdrawalDetail.getPriceUnit().multiply(new BigDecimal(100)).intValue());
			fyOrderDetail.setGoodsCatagory(orderWithdrawalDetail.getGoodsCategory());
			fyOrderDetailList.add(fyOrderDetail);
		}
		/*List<SrmWithdrawalMain> srmWithdrawalMainList = new ArrayList<>();
		for(String s : hashSet){
			//主单状态以及商户订单号更新List
			SrmWithdrawalMain srmWithdrawalMain = new SrmWithdrawalMain();
			srmWithdrawalMain.setWithdrawalCode(s);
			srmWithdrawalMain.setMchntOrderId(mchntOrderId);
			srmWithdrawalMain.setDataStatus("2");
			srmWithdrawalMainList.add(srmWithdrawalMain);
		}*/
		srmWithdrawalBatch.setTotalAmount(totalAmount);
		fyOrder.setOrderAmt(totalAmount.multiply(new BigDecimal(100)).intValue());

		/**
		 * 进行数据的保存和更新，进行事务的控制
		 */
		TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionAttribute());
		try {
			// 插入t_srm_withdrawal_batch
			srmWithdrawalBatchDao.insert(srmWithdrawalBatch);

			/*// 更新t_srm_withdrawal_main
			for(SrmWithdrawalMain srmWithdrawalMain : srmWithdrawalMainList) {
				srmWithdrawalMainDao.updateStatusOrMchntOrderIdByWithdrawalCode(srmWithdrawalMain);
			}
*/
			// 插入t_srm_withdrawal_detail_split
			if(hashSet.size() > 0){
				srmWithdrawalDetailSplitDao.updateDocStatusABatchByMchntId(hashSet,mchntOrderId,0);
			}

			// 插入t_fy_order
			fyOrderService.insert(fyOrder);

			// 插入t_fy_order_detail
			if(fyOrderDetailList.size() > 0) {
				fyOrderDetailService.insetList(fyOrderDetailList);
			}

			transactionManager.commit(transaction);
		} catch (Exception e) {
			transactionManager.rollback(transaction);
			throw new Exception("操作失败！"  + ExceptionUtils.getMessage(e));
		}finally{
			if(transaction!=null && !transaction.isCompleted()){
				transactionManager.rollback(transaction);
			}
		}

	}
	
	/**
	 * 拆分子单提现单（1:采购成本 2:推广费 3:运费）
	 * @param srmWithdrawalDetail
	 * @return
	 */
	private List<SrmWithdrawalDetailSplit> splitSrmWithdrawalDetail(String poOrderCode, SrmWithdrawalDetail srmWithdrawalDetail) {
		
		List<SrmWithdrawalDetailSplit> srmWithdrawalDetailSplitList = new ArrayList<>();
		
		// 待拆分总金额
		BigDecimal totalSpiltAmount = srmWithdrawalDetail.getWithdrawalTotalAmount();

		BigDecimal tempInstoreCostSpiltAmount = new BigDecimal("200000");
		//拆分笔数
		BigDecimal spiltNo = totalSpiltAmount.divide(tempInstoreCostSpiltAmount,0,BigDecimal.ROUND_UP);

		if(totalSpiltAmount.compareTo(BigDecimal.ZERO) == 0){
			sendDingding(srmWithdrawalDetail.getBillDetailCode()+"", "提现明细数据分摊异常",1);
		}

		//执行拆分金额
		BigDecimal spiltAmount = totalSpiltAmount.divide(spiltNo,2,BigDecimal.ROUND_UP);
		//执行拆分数量
		BigDecimal spiltNum = new BigDecimal(srmWithdrawalDetail.getQty()).divide(spiltNo, 0, BigDecimal.ROUND_UP);

		List<BigDecimal> spiltAmountList = splitData(totalSpiltAmount, spiltAmount);

		List<BigDecimal> spiltNumList = splitData(BigDecimal.valueOf(srmWithdrawalDetail.getQty()), spiltNum);

		if(spiltAmountList.size() != spiltNumList.size()){
			sendDingding(srmWithdrawalDetail.getBillDetailCode()+"", "提现明细数据分摊异常",1);
		}

		if(spiltAmountList != null && spiltAmountList.size() > 0) {
			int i = 0;
			for(BigDecimal amountSplit : spiltAmountList) {
				SrmWithdrawalDetailSplit srmWithdrawalDetailSplit1 = new SrmWithdrawalDetailSplit();
				srmWithdrawalDetailSplit1.setWithdrawalCode(srmWithdrawalDetail.getWithdrawalCode());
				srmWithdrawalDetailSplit1.setBillDetailCode(srmWithdrawalDetail.getBillDetailCode());
				srmWithdrawalDetailSplit1.setMchntDetailOrderId(MchntIdGeneratorUtils.getMchntChildOrderId(srmWithdrawalDetail.getPoOrderCode()));
				srmWithdrawalDetailSplit1.setPoOrderCode(srmWithdrawalDetail.getPoOrderCode());
				srmWithdrawalDetailSplit1.setFyAccountCode(srmWithdrawalDetail.getFyAccountCode());
				srmWithdrawalDetailSplit1.setWithdrawalType(srmWithdrawalDetail.getWithdrawalType());
				srmWithdrawalDetailSplit1.setSplitAmount(amountSplit);
				srmWithdrawalDetailSplit1.setDocumentStatus(9);
				BigDecimal priceUnit = amountSplit.divide(BigDecimal.valueOf(spiltNumList.get(i).intValue()),2,BigDecimal.ROUND_UP);
				srmWithdrawalDetailSplit1.setSplitPriceUnit(priceUnit);
				srmWithdrawalDetailSplit1.setSplitQty(spiltNumList.get(i).intValue());
				srmWithdrawalDetailSplitList.add(srmWithdrawalDetailSplit1);
				i++;
			}
		}
		
		return srmWithdrawalDetailSplitList;
	}


	/**
	 * 将费用按照指定的费用拆分
	 * @param totalValue
	 * @return
	 */
	private static List<BigDecimal> splitData(BigDecimal totalValue, BigDecimal spiltValue) {
		List<BigDecimal> result = new ArrayList<>();
		if(totalValue != null && totalValue.compareTo(new BigDecimal(0)) == 1) {
			if(totalValue.compareTo(spiltValue) < 1) {
				result.add(totalValue);
			}else if(totalValue.doubleValue() > spiltValue.doubleValue()) {
				boolean flag = true;
				BigDecimal tempValue = totalValue;
				result.add(spiltValue);
				while(flag) {
					tempValue = tempValue.subtract(spiltValue);
					if(tempValue.compareTo(spiltValue) == 1) {
						result.add(spiltValue);
					}else {
						result.add(tempValue);
						flag = false;
					}
				}
			}
		}
		return result;
	}
	
	private void sendDingding(String receiptId, String message, int dingdingtype) {
		String title = "异常通知";
		StringBuffer text = new StringBuffer();
		if(dingdingtype == 0){
			text.append("> 异常供应商ID：" + receiptId + "  \n  " +
					"> 异常原因：" + message + "  \n  " +
					"> \n\n  ");
		}else{
			text.append("> 异常提现子单Code：" + receiptId + "  \n  " +
					"> 异常原因：" + message + "  \n  " +
					"> \n\n  ");
		}

		// 获取执行的token
		JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
		if("200".equals(tokenResult.getCode())) {
			String token = tokenResult.getData().toString();
			// 生成定时消息推送
			OigMarkdownMessageDTO oigMarkdownMessageDTO = new OigMarkdownMessageDTO(fyWarningClientId, title, text.toString(), null);
			try {
				dingdingFeignService.sendMarkdown(token, oigMarkdownMessageDTO);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
