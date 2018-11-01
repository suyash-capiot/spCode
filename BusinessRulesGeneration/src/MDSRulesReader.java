import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.auth.BasicUserPrincipal;

import oracle.mds.core.IsolationLevel;
import oracle.mds.internal.util.MDSDebugLogLevel;
import oracle.mds.internal.util.MDSLogLevel;
import oracle.mds.internal.util.MDSLogger;
import oracle.mds.internal.util.MDSTracer;
import oracle.rules.sdk2.datamodel.BucketSetTable;
import oracle.rules.sdk2.datamodel.DataModel;
import oracle.rules.sdk2.decisiontable.Bucket;
import oracle.rules.sdk2.decisiontable.BucketSet;
import oracle.rules.sdk2.decisiontable.BucketTable;
import oracle.rules.sdk2.decisiontable.DTAction;
import oracle.rules.sdk2.decisiontable.DTActionNode;
import oracle.rules.sdk2.decisiontable.DTActionNodeTable;
import oracle.rules.sdk2.decisiontable.DTRule;
import oracle.rules.sdk2.decisiontable.DTRuleTable;
import oracle.rules.sdk2.decisiontable.Dimension;
import oracle.rules.sdk2.decisiontable.DimensionNode;
import oracle.rules.sdk2.decisiontable.DimensionNodeTable;
import oracle.rules.sdk2.decisiontable.DimensionTable;
import oracle.rules.sdk2.decisiontable.RuleSheet;
import oracle.rules.sdk2.dictionary.AbstractDictionaryFinder;
import oracle.rules.sdk2.dictionary.DictionaryFinder;
import oracle.rules.sdk2.dictionary.IDTable;
import oracle.rules.sdk2.dictionary.RuleDictionary;
import oracle.rules.sdk2.exception.RepositoryException;
import oracle.rules.sdk2.exception.SDKException;
import oracle.rules.sdk2.exception.SDKWarning;
import oracle.rules.sdk2.repository.DictionaryExplorer;
import oracle.rules.sdk2.repository.DictionaryFQN;
import oracle.rules.sdk2.repository.DictionaryUpdater;
import oracle.rules.sdk2.repository.MDSRepository;
import oracle.rules.sdk2.repository.MDSSandbox;
import oracle.rules.sdk2.repository.Repositories;
import oracle.rules.sdk2.ruleset.Expression;
import oracle.rules.sdk2.ruleset.ExpressionTable;
import oracle.rules.sdk2.ruleset.Rule;
import oracle.rules.sdk2.ruleset.RuleSet;
import oracle.rules.sdk2.ruleset.RuleSheetTable;
import oracle.rules.sdk2.ruleset.RuleTable;

public class MDSRulesReader {

	//private static String rulesLocation = "/soa/shared/mediator/oracle/rules/oracle/tip/mediator/rules/dictionary/DynamicRouting.rules";
	private static String rulesLocation = "/deployed-composites/default/BRE_CNK_rev1.0/oracle/rules/bre_cnk/FareCalculationsBusinessRules.rules";
	
	private static String SANDBOX_NAME = "A_VERY_UNQ_SBOX";
	
	private static String CONDITION_NAME = "Root$BusinessRuleIntake$CommonElements.supplierMarket";
	private static String MARKET_INDIA = "\"India\"";
	private static String MARKET_SINGAPORE = "\"Singapore\"";
	private static String EMPTY_VALUE = "";
	private static String RULESET_NAME = "FareCalculationsRuleset_Standard";
	private static String DT_NAME = "CommonFeaturesDT";
	
	private static String USER_PRINCIPAL = "shrikantp";
	
	public static void main(String[] args) {
		boolean isDirty = false;
		
		DictionaryUpdater dictUpdater = null;
		MDSSandbox mdsSandbox = null;
		try {
			MDSRepository mdsRepo = Repositories.getRepository();
			
			DictionaryExplorer dictExplorer = mdsRepo.getExplorer(null, null);
			List<String> dictList = dictExplorer.list(null, true);
			for (String dictName : dictList) {
				System.out.println(dictName);
			}
			
			System.out.println("***** MDSRepository.isSandboxSupported()=" + mdsRepo.isSandboxSupported());
			mdsSandbox = mdsRepo.getSandboxSupport();
			List<String> sndBoxes = mdsSandbox.listSandboxes();
			if (sndBoxes.contains(SANDBOX_NAME) == false) {
				mdsSandbox.createSandbox(SANDBOX_NAME);
			}
//			else {
//				mdsSandbox.abortSandbox(SANDBOX_NAME);
//				mdsSandbox.createSandbox(SANDBOX_NAME);
//			}
			
			for (String sndBox : sndBoxes) {
				System.out.printf("### Found sandbox <%s>\n", sndBox);
			}
			
			//AbstractDictionaryFinder dictFinder = mdsRepo.getFinder(null, false, SANDBOX_NAME);
			AbstractDictionaryFinder dictFinder = mdsRepo.getFinder(null);
			//RuleDictionary rld = dictFinder.findDictionaryWithThisFinder(rulesLocation);
			//dictUpdater = mdsRepo.getUpdater(null, null);
			
			BasicUserPrincipal userPrincipal = new BasicUserPrincipal(USER_PRINCIPAL);
			//dictUpdater = mdsRepo.getUpdater(null, null, false, SANDBOX_NAME);
			dictUpdater = mdsRepo.getUpdater(null, userPrincipal, false, SANDBOX_NAME);
			RuleDictionary rld = dictUpdater.loadForUpdate(rulesLocation, dictFinder);
			
			//isDirty = printRuleDictionary(rld);
			//printRuleDictionary(rld);
			//isDirty = isDirty || addRule(rld);
			//printRuleDictionary(rld);
			//System.out.println("Committing sandbox " + SANDBOX_NAME);
			//mdsSandbox.commitSandbox(SANDBOX_NAME);
			//isDirty = isDirty || removeRuleAddedByClass(rld);
			
			//isDirty = addRule(rld);
			
			if (isDirty) {
				try {
					ArrayList<SDKWarning> sdkWarns = new ArrayList<SDKWarning>(); 
					rld.update(sdkWarns);
					System.out.println("\n\n===== Warnings =====");
					for (SDKWarning sdkWarn : sdkWarns) {
						System.out.printf("Warning:<%s> at <%s>\n", sdkWarn.getMessage(), sdkWarn.getDictionaryObject().toString());
					}
					dictUpdater.updateDictionary(rulesLocation, rld);
//					mdsSandbox.commitSandbox(SANDBOX_NAME);
				}
				catch (Exception x) {
					x.printStackTrace();
				}
			}

		}
		catch (Exception ex) {
			ex.printStackTrace();

			if (dictUpdater != null) {
				try {
					dictUpdater.abortUpdate(rulesLocation);
				}
				catch (Exception x) {
					x.printStackTrace();
				}
			}
			
//			if (mdsSandbox != null) {
//				try {
//					mdsSandbox.abortSandbox(SANDBOX_NAME);
//				}
//				catch (Exception x) {
//					x.printStackTrace();
//				}
//			}
		}
		
		System.exit(0);
	}

	private static boolean printRuleDictionary(RuleDictionary rld) {
		boolean isDirty = false;
		List<RuleSet> ruleSets = rld.getRuleSets(false);
		for (RuleSet ruleSet : ruleSets) {
			System.out.println("\tFound ruleset " + ruleSet.getName());
			RuleSheetTable decisionTablesTable = ruleSet.getRuleSheetTable();
			Iterator<RuleSheet> decisionTables = decisionTablesTable.iterator();
			while (decisionTables.hasNext()) {
				RuleSheet decisionTable = decisionTables.next();
				System.out.println("\t\tFound Decision Table " + decisionTable.getName());
				DTRuleTable dtRuleTable = decisionTable.getDTRuleTable();
				
				System.out.println("======================================================================================================================");
				for (int i=0; i < dtRuleTable.size(); i++) {
					printRule(dtRuleTable.get(i));
				}
				
			}
		}
		
		DataModel dataModel = rld.getDataModel();
		BucketSetTable valSetTable= dataModel.getBucketSetTable();
		Iterator<BucketSet> valSets = valSetTable.iterator();
		while (valSets.hasNext()) {
			BucketSet valSet = valSets.next();
			System.out.println("\tFound Value Set " + valSet.getName());
			BucketTable valsTable = valSet.getBucketTable();
			Iterator<Bucket> vals = valsTable.iterator();
			while (vals.hasNext()) {
				Bucket val = vals.next();
				System.out.println("\t\tFound value " + val.getAlias());
			}
		}
		
		return isDirty;
	}
	
	private static boolean printDimensionNode(DimensionNode dimNode) {
		boolean isDirty = false;
		String dimCondition = dimNode.getDimension().getValue();
		String[] dimNodeVals = dimNode.getValues();
		System.out.printf("\t\t\tFound DimensionNode with dimension=<%s> and values=<%s>\n", dimCondition, Arrays.toString(dimNodeVals));
		
		System.out.println("\t\t\t\t********** BucketSet Contents");
		BucketSet valSet = dimNode.getBucketSet();
		List<Bucket> valsList = valSet.getBuckets();
		Iterator<Bucket> valsIter = valsList.iterator();
		while (valsIter.hasNext()) {
			Bucket val = valsIter.next();
			System.out.printf("\t\t\t\tFound Bucket with alias=<%s>, name=<%s>, exprvalue=<%s>\n", val.getAlias(), val.getName(), val.getExpression().getValue());
		}
		
		System.out.println("\n\n\t\t\t\t*********** IDTable<Bucket> Contents");
		
		IDTable<Bucket> valsTable = dimNode.getBuckets();
		Iterator<Bucket> vals = valsTable.iterator();
		while (vals.hasNext()) {
			Bucket val = vals.next();
			System.out.printf("\t\t\t\tFound Bucket with alias=<%s>, name=<%s>, exprvalue=<%s>\n", val.getAlias(), val.getName(), val.getExpression().getValue());
		}
		
		System.out.println("\n\n");
		
		try {
			if (valsTable.size() == 1) {
				Bucket valOne = valsTable.get(0);
				if (MARKET_INDIA.equals(valOne.getExpression().getValue())) {
					BucketTable valSetTable = valSet.getBucketTable();
					Bucket valSingapore = valSetTable.getByName(MARKET_SINGAPORE);
					if (valSingapore == null) {
						valSingapore = valSet.add(MARKET_SINGAPORE).init();
						valSingapore.setAlias(MARKET_SINGAPORE);
						valSingapore.setName(MARKET_SINGAPORE);
						Expression expr = valSingapore.getExpression();
						if (expr != null) {
							expr.setValue(MARKET_SINGAPORE);										
						}
					}
					else {
						// Spoof an update to BucketSet so that changes to valsTable are saved to RuleDictionary
						valSet.add(EMPTY_VALUE);
						valSet.remove(EMPTY_VALUE);
					}
					
					valsTable.add(valSingapore);
					isDirty = true;
				}
			}
		}
		catch (Exception x) {
			x.printStackTrace();
		}
		
		return isDirty;
	}

	private static boolean printRule(DTRule dtRule) {
		boolean isDirty = false;
		System.out.printf("\t\tFound rule with alias=<%s>, name=<%s>\n", dtRule.getAlias(), dtRule.getName());
		DimensionNodeTable dimNodeTbl = dtRule.getDimensionNodeTable();
		for (int ruleIdx=0; ruleIdx < dimNodeTbl.size(); ruleIdx++) {
			DimensionNode dimNode = dimNodeTbl.get(ruleIdx);
			isDirty = isDirty || printDimensionNode(dimNode);
		}

		DTActionNodeTable actNodeTbl = dtRule.getDTActionNodeTable();
		if (actNodeTbl == null) {
			System.out.println("\t\t%%%%%%%%%% No Actions");
		}
		else {
			for (int actIdx=0; actIdx < actNodeTbl.size(); actIdx++) {
				DTActionNode dtActionNode = actNodeTbl.get(actIdx);
				DTAction dtAct = dtActionNode.getDTAction();
				ExpressionTable exprTbl = dtActionNode.getExpressionTable();
				for (int exprIdx=0; exprIdx < exprTbl.size(); exprIdx++) {
					Expression expr = exprTbl.get(exprIdx);
					System.out.printf("\t\tFound action=<%s> and expression with actionParameter=<%s>, value=<%s>\n", dtAct.getForm(), expr.getDTActionParameterName(), expr.getValue());
				}
			}
		}

		System.out.println("\n\n ---------- XXX ----------");
		
		return isDirty;
	}
	
	private static boolean addRule(RuleDictionary rld) throws SDKException {
		boolean isDirty = false;
		Bucket valBkt = null;
		DimensionNode dimNode = null;
		DTActionNode dtActNode = null;
		DTAction dtAct = null;
		ExpressionTable exprTbl = null;
		Expression expr = null;
		
		RuleSet rlSet = rld.getRuleSet(RULESET_NAME);
		if (rlSet != null) {
			RuleSheet rlSheet = rlSet.getRuleSheetTable().getByName(DT_NAME);
			if (rlSheet != null) {
				DTRuleTable dtRlTbl = rlSheet.getDTRuleTable();
				int currSize = dtRlTbl.size();
				
				DTRule dtRl = dtRlTbl.add();
				
				dimNode = dtRl.getDimensionNodeTable().get(0);
				valBkt = dimNode.getBucketSet().getBucketTable().getByName("\"Galileo\"");
				dimNode.getBuckets().set(0, valBkt);
				dimNode.setValues(valBkt.getExpression().getValue());
				printDimensionNode(dimNode);
				
				dimNode = dtRl.getDimensionNodeTable().get(1);
				valBkt = dimNode.getBucketSet().getBucketTable().getByName("\"Australia\"");
				dimNode.getBuckets().set(0, valBkt);
				dimNode.setValues(valBkt.getExpression().getValue());
				printDimensionNode(dimNode);
				
				dimNode = dtRl.getDimensionNodeTable().get(2);
				valBkt = dimNode.getBucketSet().getBucketTable().getByName("\"SpiceJet\"");
				dimNode.getBuckets().set(0, valBkt);
				dimNode.setValues(valBkt.getExpression().getValue());
				printDimensionNode(dimNode);
				
				dimNode = dtRl.getDimensionNodeTable().get(3);
				valBkt = dimNode.getBucketSet().add("[\"2016-09-01T00:00:00@Asia/Calcutta\"..\"2017-03-31T23:59:59@Asia/Calcutta\")");
				dimNode.getBuckets().set(0, valBkt);
				dimNode.setValues(valBkt.getName());
				printDimensionNode(dimNode);
				
				DTActionNodeTable dtActTbl = dtRl.getDTActionNodeTable();
				dtActNode = dtActTbl.get(0);
				dtAct = dtActNode.getDTAction();
				expr = dtAct.getExpression(0);
				expr.setDTActionParameterName("parameter0");
				expr.setValue("Root$BusinessRuleIntake$CommonElements.selectedRow = \"Standard_R" + currSize + "\"");
				
				dtActNode = dtActTbl.get(1);
				dtAct = dtActNode.getDTAction();
				expr = dtAct.getExpressionByDTActionParameterName("ruleFlowName");
				expr.setValue("RL.get firing rule name()");
				
				dtRl.getOverride().add(dtRlTbl.get(currSize - 1));
				
				currSize = dtRlTbl.size();
				dtRlTbl.move((currSize - 1), (currSize - 2), false);
				
				isDirty = true;
			}
		}
		return isDirty;
	}
	
	private static boolean removeRuleAddedByClass(RuleDictionary rld) {
		boolean isDirty = false;
		
		RuleSet rlSet = rld.getRuleSet(RULESET_NAME);
		if (rlSet != null) {
			RuleSheet rlSheet = rlSet.getRuleSheetTable().getByName(DT_NAME);
			if (rlSheet != null) {
				DTRuleTable dtRlTbl = rlSheet.getDTRuleTable();
				if (dtRlTbl.size() > 5) {
					dtRlTbl.remove(5);
					isDirty = true;
				}
			}
		}
		
		return isDirty;
	}
	
}
