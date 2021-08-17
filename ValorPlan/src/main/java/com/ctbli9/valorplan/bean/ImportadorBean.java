package com.ctbli9.valorplan.bean;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.importa.Importador;
import com.ctbli9.valorplan.modelo.importa.TipoLayout;
import com.ctbli9.valorplan.negocio.importar.ImportadorService;
import com.ctbli9.valorplan.negocio.importar.ImportarCadastro;
import com.ctbli9.valorplan.negocio.importar.ImportarOrcamento;
import com.ctbli9.valorplan.negocio.importar.ImportarOrcamentoAgr;
import com.ctbli9.valorplan.recursos.LibUtilFaces;

import ctbli9.adm.modelo.CoContrat;
import ctbli9.recursos.ArquivoTexto;
import ctbli9.recursos.DataUtil;
import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.LibUtil;
import ctbli9.recursos.RegraNegocioException;

@ManagedBean(name="importadorBean")
@ViewScoped
public class ImportadorBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Importador formatoDados;
    private List<String> listaArquivos;
	private String arquivoImporta = null;
	
	private FacesMessages msg;
	
	/*
	 * Construtor
	 */
	public ImportadorBean() {
		this.msg = new FacesMessages();
		this.formatoDados = new Importador();
	}

	/*
	 * Atributos
	 */
	public Importador getFormatoDados() {
		return formatoDados;
	}
	public void setFormatoDados(Importador dados) {
		this.formatoDados = dados;
	}
	
    public List<String> getListaArquivos() {
		return listaArquivos;
	}
    
    public String getArquivoImporta() {
		return arquivoImporta;
	}
    public void setArquivoImporta(String arquivoImporta) {
		this.arquivoImporta = arquivoImporta;
	}
	    
	public boolean isItemSelecionado() {
		return this.arquivoImporta != null && !this.arquivoImporta.isEmpty();
	}


	/*
	 * Metodos
	 */
	public void listarArquivos() {
		String caminho = LibUtil.localArquivoIntegra();
		File file = new File(caminho);
		
		File[] arquivos = file.listFiles();
		
		listaArquivos = new ArrayList<String>();
		
		for (int i = 0; i < arquivos.length; i++) {
			if (!arquivos[i].getName().endsWith("PROCESSADO") && (arquivos[i].getName().indexOf(".PROCES_") == -1))
				listaArquivos.add(arquivos[i].getName());
		}
		
	}
	
	public void importarArquivo(ActionEvent event) {
		ImportadorService service = null;
		
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			if (this.arquivoImporta == null)
				throw new RegraNegocioException("Nenhum arquivo selecionado.");
			
			service = new ImportadorService();
			service.setFormatoDados(this.formatoDados);
			String erro = service.verificaTipoArquivo(this.arquivoImporta, this.formatoDados);
			if (erro.isEmpty()) {
				String arqErro = null;
				
				if (this.formatoDados.getTipo().equals(TipoLayout.CADASTRO))
					arqErro = new ImportarCadastro(con, service).
							importarArquivo(LibUtil.localArquivoIntegra() + this.arquivoImporta);
				else {
					CoContrat contrato = LibUtil.getUsuarioSessao().getContrato();
					if (contrato.getNmContrato().equals("agrinorte"))
						arqErro = new ImportarOrcamentoAgr(con, service).
							importarArquivo(LibUtil.localArquivoIntegra() + this.arquivoImporta);
					else
						arqErro = new ImportarOrcamento(con, service).
							importarArquivo(LibUtil.localArquivoIntegra() + this.arquivoImporta);
				}
				
				// Renomeia o arquivo
				ArquivoTexto arqTmp = new ArquivoTexto(LibUtil.localArquivoIntegra() + this.arquivoImporta);
				while (!arqTmp.renomeiaArquivo(LibUtil.localArquivoIntegra() + this.arquivoImporta + 
						".PROCES_" + 
						DataUtil.dataString(new Date(), "yyyy_MM_dd-HH_mm_ss"))) {
					
					try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) {}
				}
				arqTmp = null;
				
				listarArquivos();
				
				msg.info("Importação realizada com sucesso!");
				if (!arqErro.isEmpty()) {
					this.msg.erro("Ocorreram erros na importação. Verifique lista das linhas com erro.");
					LibUtilFaces.abreRelatorio(arqErro);
				}				
			} else {
				msg.erro(erro);
			}			
			
		} catch (Exception e) {
			ConexaoDB.desfazerTransacao(con);
			e.printStackTrace();
			msg.erro("ERRO de Importação.\n" + e.getMessage());
			
		} finally {
			service = null;
			ConexaoDB.close(con);
		}
	}//importar
}
