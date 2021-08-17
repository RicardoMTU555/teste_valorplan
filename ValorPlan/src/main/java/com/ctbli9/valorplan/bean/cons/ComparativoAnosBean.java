
package com.ctbli9.valorplan.bean.cons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.orc.ComparativoAno;
import com.ctbli9.valorplan.negocio.consulta.ComparativoAnoService;

import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.RegraNegocioException;

@ManagedBean(name = "comparativoAnosBean")
@ViewScoped
public class ComparativoAnosBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<ComparativoAno> comparativoAno = new ArrayList<>();
	
	private FacesMessages msg = new FacesMessages();

    /*
	 * Atributos
	 */
    
    public List<ComparativoAno> getComparativoAno() {
    	return comparativoAno;
    }

	
	/*
	 * Metodos
	 */
	
    public void listarComparativo() {
    	
    	ConexaoDB con = null;
    	ComparativoAnoService servico;    	
    	try {
			con = new ConexaoDB();
			servico = new ComparativoAnoService(con);				
			this.comparativoAno = servico.listarContasTotalAno();
			servico = null;
			ConexaoDB.gravarTransacao(con);
			
			this.msg.info("Gerado o comparativo do CENÁRIO Atual em relação ao ano anterior.");
			
		} catch (RegraNegocioException e) {
			ConexaoDB.desfazerTransacao(con);
			this.msg.erro(e.getMessage());
		} catch (Exception e) {
			ConexaoDB.desfazerTransacao(con);
			this.msg.erro(e.getMessage());
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
    }
    

    public String getCabecalhoAnterior() {
    	return "Realizado " + (PlanoServiceDAO.getPlanoSelecionado().getNrAno() - 1);
    }
	
    public String getCabecalhoAtual() {
    	return "Orçado " + PlanoServiceDAO.getPlanoSelecionado().getNrAno();
    }
	
}

