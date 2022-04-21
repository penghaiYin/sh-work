package com.yph.shwork.fuiou.dto;

public class JdyFyTransferFailDto {

	private JdyValueDto transfer_no;
	private JdyValueDto fail_reason;
	private JdyValueDto supplier_name;
	private JdyValueDto original_payee;
	private JdyValueDto original_bank_card_no;
	private JdyValueDto original_id_card;

	public JdyValueDto getTransfer_no() {
		return transfer_no;
	}

	public void setTransfer_no(JdyValueDto transfer_no) {
		this.transfer_no = transfer_no;
	}

	public JdyValueDto getSupplier_name() {
		return supplier_name;
	}

	public void setSupplier_name(JdyValueDto supplier_name) {
		this.supplier_name = supplier_name;
	}

	public JdyValueDto getOriginal_payee() {
		return original_payee;
	}

	public void setOriginal_payee(JdyValueDto original_payee) {
		this.original_payee = original_payee;
	}

	public JdyValueDto getOriginal_bank_card_no() {
		return original_bank_card_no;
	}

	public void setOriginal_bank_card_no(JdyValueDto original_bank_card_no) {
		this.original_bank_card_no = original_bank_card_no;
	}

	public JdyValueDto getOriginal_id_card() {
		return original_id_card;
	}

	public void setOriginal_id_card(JdyValueDto original_id_card) {
		this.original_id_card = original_id_card;
	}

	public JdyValueDto getFail_reason() {
		return fail_reason;
	}

	public void setFail_reason(JdyValueDto fail_reason) {
		this.fail_reason = fail_reason;
	}
}
