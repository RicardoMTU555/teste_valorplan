package com.ctbli9.valorplan.bean.cons;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.ResumoService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.recursos.DataUtil;
import ctbli9.recursos.FacesMessages;

@ManagedBean(name = "consultaRecDesBean")
@ViewScoped
public class ConsultaRecDeducaoBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private FacesMessages msg = new FacesMessages();
	private StreamedContent file;
	private String nomeXLS;
	private boolean processoConcluido;

	public ConsultaRecDeducaoBean() {
		this.processoConcluido = false;
	}
	
	public boolean isProcessoConcluido() {
		return processoConcluido;
	}
	
	public void baixarPlanilhaGeral() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "ReceitaDeducoes_" + DataUtil.dataString(new Date(), "yyyy_MM_dd") + ".xls";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			new ResumoService(con).gerarPlanilhaRecDes(caminho);
			
			msg.info("Planilha gerada.");
			this.processoConcluido = true;
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public StreamedContent getFile() throws FileNotFoundException {
		String caminhoWebInf = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
		caminhoWebInf = caminhoWebInf + "relatorios/" + this.nomeXLS;
		InputStream stream = new FileInputStream(caminhoWebInf); //Caminho onde esta salvo o arquivo.
		
		file = DefaultStreamedContent.builder()
                .name(this.nomeXLS)
                .contentType("application/xls")
                .stream(() -> stream)
                .build();  

        return file;  
    } 
	
}
