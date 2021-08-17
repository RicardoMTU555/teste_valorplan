package com.ctbli9.valorplan.negocio;

import java.util.List;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.modelo.FluxoMes;

public class FluxoService {

	public TreeNode criaArvoreFluxo(List<FluxoMes> listaFluxo) {
		FluxoMes fluxo = new FluxoMes("FLUXO DE CAIXA", "FLUXO", 12);
		
		TreeNode raiz = new DefaultTreeNode(fluxo, null);
		TreeNode elo = null;
		
		for (FluxoMes fluxoMes : listaFluxo) {
			if (fluxoMes.getNatureza().equals("TOTAL")) {
				elo = new DefaultTreeNode(fluxoMes, raiz);
			} else {
				new DefaultTreeNode(fluxoMes, elo);
			}
		}

		return raiz;
	}

}
