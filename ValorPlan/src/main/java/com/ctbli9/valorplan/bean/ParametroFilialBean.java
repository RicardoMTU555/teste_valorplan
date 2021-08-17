package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.modelo.Filial;
import ctbli9.negocio.FilialService;
import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.email.EmailService;
import ctbli9.recursos.email.ParametroEmail;

@ManagedBean(name="paramFilialBean")
@ViewScoped
public class ParametroFilialBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<Filial> filiais;
	private Filial filial;
	private ParametroEmail parametro;
	
	private FacesMessages msg = new FacesMessages();
	
	public List<Filial> getFiliais() {
		return filiais;
	}
	public Filial getFilial() {
		return filial;
	}
	public void setFilial(Filial filial) {
		this.filial = filial;
	}
	public ParametroEmail getParametro() {
		return parametro;
	}
	public void setParametro(ParametroEmail parametro) {
		this.parametro = parametro;
	}
	
	public void inicia() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.filiais = new FilialService(con.getConexao()).listar();
			
    		this.filial = Global.getFuncionarioLogado().getCenCusto().getFilial();
    			
			this.parametro = new EmailService().getEmail(con.getConexao(), this.filial.getCdFilial());
    		if (this.parametro == null) {
    			this.parametro = new ParametroEmail();
    		}
    		
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);		
		} 
	}
	
	public void carregarEmail() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();

    		this.parametro = new EmailService().getEmail(con.getConexao(), this.filial.getCdFilial());
    		if (this.parametro == null) {
    			this.parametro = new ParametroEmail();
    		}
    		
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);		
		} 
	}
	
	public void gravar() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();

    		new EmailService().gravarEmail(con.getConexao(), this.parametro, this.filial.getCdFilial());

    		this.msg.info("Servidor de Email Salvo com sucesso.");
    		
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);		
		} 

	}
	
}
