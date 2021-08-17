package com.ctbli9.valorplan.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.file.UploadedFile;

import ctbli9.recursos.LibUtil;

@ManagedBean(name="enviaArquivoBean")
@ViewScoped
public class EnviaArquivoBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

    private UploadedFile uploadedFile;
    private List<String> listaArquivos;

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }
    public void setUploadedFile(UploadedFile file) {
        this.uploadedFile = file;
    }
    public List<String> getListaArquivos() {
		return listaArquivos;
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
    
    public void upload() {
    	try {
			String caminho = LibUtil.localArquivoIntegra();
			
			String arquivoOriginal = uploadedFile.getFileName();
			arquivoOriginal = arquivoOriginal.substring(arquivoOriginal.lastIndexOf("\\") + 1);
						
			File file = new File(caminho, arquivoOriginal);
			 
		    OutputStream out = new FileOutputStream(file);
		    out.write(uploadedFile.getContent());
		    out.close();
		 
		    FacesContext.getCurrentInstance().addMessage(
		               null, new FacesMessage("Upload completo", 
		               "O arquivo " + uploadedFile.getFileName() + " foi salvo!"));
		    
		    listarArquivos();
		    
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
		              null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Erro", e.getMessage()));
		}    	
    }//upload
    
}
