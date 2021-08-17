package com.ctbli9.valorplan.recursos;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.negocio.FuncionarioService;

import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.LibUtil;

public class Global {
	
	public static Funcionario getFuncionarioLogado() {
		return (Funcionario) LibUtil.getParametroSessao("FUNC_LOGADO");
	}
	
	public static void setFuncionarioLogado() {
		ConexaoDB con = null;
		try {
    		con = new ConexaoDB();
    		Funcionario funcionarioLogado = new FuncionarioService(con.getConexao()).pesquisarPorLogin();
    		ConexaoDB.gravarTransacao(con);
    		LibUtil.setParametroSessao("FUNC_LOGADO", funcionarioLogado);
    	} catch (Exception e) {
    		if (!e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
    			e.printStackTrace();
    		else
    			System.out.println(e.getMessage());
			ConexaoDB.desfazerTransacao(con);
			LibUtil.setParametroSessao("FUNC_LOGADO", new Funcionario());
			
		} finally {
			ConexaoDB.close(con);
		}
	}

	public static void erro(ConexaoDB con, Exception e, FacesMessages msg, String texto) {
		if (texto == null || texto.isEmpty())
			texto = "ERRO: ";
		
		if (con != null && con.getConexao() != null)
			ConexaoDB.desfazerTransacao(con);
		
		if (!e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
			e.printStackTrace();
		
		if (e.getMessage() == null)
			texto += e.getClass().getName().toString();
		else
			texto += tratarMensagem(e.getMessage());
		
		if (!e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException")) {
			
			for (StackTraceElement stack : e.getStackTrace()) {
	
				if (stack.getClassName().toString().startsWith("com.ctbli9.valorplan")) {
					texto += " => LOCAL: " + stack.getFileName() +
							" => METODO: " + stack.getMethodName() + 
							" => LINHA: " + stack.getLineNumber();
					break;
				}
			}
		}
		msg.erro(texto);
	}
	
	private static String tratarMensagem(String msg) {
		if (msg.contains("FOREIGN KEY")) {
			String[] partes = msg.split(" ");
			
			for (int i = 0; i < partes.length; i++) {
				if (partes[i].equalsIgnoreCase("table")) {
					msg = "Registro não pode ser deletado pois existe na tabela " + partes[i+1];
					break;
				}
			}
		}
		return msg;
	}
	
}
