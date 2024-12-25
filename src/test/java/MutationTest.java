import ast.*;
import ast.Cmd;
import ast.ExprBinary;
import ast.ExprSensor;
import mutations.Mutation;
import mutations.MutationFactory;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;

import static org.junit.jupiter.api.Assertions.*;

/** This class contains tests for the Critter parser. */
public class MutationTest {

	public static final String[] critter_programs = {"files/one-rule.txt",
			"files/one-rule-with-syntactic-sugar.txt",
			"files/example-critter-simple-1.txt",
			"files/example-critter-simple-2.txt",
			"files/one-rule-binaryoperation.txt",
			"files/draw_critter.txt",
			"files/mutated_critter_1.txt",
			"files/mutated_critter_2.txt",
			"files/mutated_critter_3.txt",
			"files/mutated_critter_4.txt",
			"files/mutated_critter_5.txt",
			"files/mutated_critter_6.txt",
			"files/unmutated_critter.txt",
			"files/example-rules.txt",
			"files/one-rule-multiple-commands.txt",
			"files/one-rule-multiple-binaryOps.txt",
			"files/one-rule-multiple-childrelations.txt"};

	protected void testAll() throws FileNotFoundException {
		testSwapMutation();
		testSwapMutationRule();
		testTransformMutation();
		testRemoveMutationForRule();
		testRemoveMutationForBinaryCondition();
		testRemoveMutationForCommand();
		testDuplicateMutationForRule();
		testDuplicateMutationForCommand();
		testReplaceMutationForBinaryCondition();
		testReplaceMutationForBinaryOperation();
		testInsertMutationForExpr();
		testInsertMutationForBinaryOperation();
		testInsertMutationForBinaryCondition();
		testInsertMutationForBinaryOperation();
		testRandomMutationsForProgram();
		testProgramClone();
	}

	@Test
	public void testSwapMutation() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[4]);
		InputStream is = new FileInputStream(critter_programs[4]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			StringBuilder prettyPgm = new StringBuilder();
			prog.prettyPrint(prettyPgm);
			System.out.print("Before applying swap mutation --> ");
			System.out.println(prettyPgm);
			Mutation sm = MutationFactory.getSwap();
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.BINARY_OPERATOR);
			for (Node node : dft) {
				ExprBinary bo = (ExprBinary) node;
				Maybe<Program> mp = sm.apply(prog, bo);
				assertTrue(mp.isPresent());
				try {
					prog = mp.get();
				} catch (NoMaybeValue ex) {
					fail("Applying swap mutation to program failed");
				}
			}

			prettyPgm = new StringBuilder();
			prog.prettyPrint(prettyPgm);
			System.out.print("After applying swap mutation --> ");
			System.out.println(prettyPgm);

			// the program produced after applying the fault should be valid
			r = new StringReader(prettyPgm.toString());
			parser = ParserFactory.getParser();
			prog = parser.parse(r);
			assertNotNull(prog);

		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testSwapMutationRule() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[14]);
		InputStream is = new FileInputStream(critter_programs[14]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			StringBuilder prettyPgm = new StringBuilder();
			prog.prettyPrint(prettyPgm);
			System.out.print("Before applying swap mutation --> ");
			System.out.println(prettyPgm);
			Mutation sm = MutationFactory.getSwap();
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.RULE);
			for (Node node : dft) {
				Rule rul = (Rule) node;
				Maybe<Program> mp = sm.apply(prog, rul);
				assertTrue(mp.isPresent());
				try {
					prog = mp.get();
				} catch (NoMaybeValue ex) {
					fail("Applying swap mutation to program failed");
				}
			}

			prettyPgm = new StringBuilder();
			prog.prettyPrint(prettyPgm);
			System.out.print("After applying swap mutation --> ");
			System.out.println(prettyPgm);

			// the program produced after applying the fault should be valid
			r = new StringReader(prettyPgm.toString());
			parser = ParserFactory.getParser();
			prog = parser.parse(r);
			assertNotNull(prog);

		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testTransformMutation() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[4]);
		InputStream is = new FileInputStream(critter_programs[4]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			StringBuilder prettyPgm = new StringBuilder();
			prog.prettyPrint(prettyPgm);
			System.out.print("Before applying transform mutation --> ");
			System.out.println(prettyPgm);
			Mutation tm = MutationFactory.getTransform();
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.BINARY_OPERATOR);
			for (Node node : dft) {
				ExprBinary bo = (ExprBinary) node;
				Maybe<Program> mp = tm.apply(prog, bo);
				assertTrue(mp.isPresent());
				try {
					prog = mp.get();
				} catch (NoMaybeValue ex) {
					fail("Applying transform mutation to program failed");
				}
			}

			prettyPgm = new StringBuilder();
			prog.prettyPrint(prettyPgm);
			System.out.print("After applying transform mutation --> ");
			System.out.println(prettyPgm);

			// the program produced after applying the fault should be valid
			r = new StringReader(prettyPgm.toString());
			parser = ParserFactory.getParser();
			prog = parser.parse(r);
			assertNotNull(prog);

		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testRemoveMutationForRule() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[5]);
		InputStream is = new FileInputStream(critter_programs[5]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			// the program should have 6 rules
			assertEquals(6, prog.getChildren().size());
			Maybe<Node> mn = prog.findNodeOfType(NodeCategory.RULE);
			assertTrue(mn.isPresent());
			try {
				Rule rul = (Rule) mn.get();
				Mutation rm = MutationFactory.getRemove();
				Maybe<Program> mp = rm.apply(prog, rul);
				assertTrue(mp.isPresent());
				prog = mp.get();
				// the program should have 5 rules
				assertEquals(5, prog.getChildren().size());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				StringBuilder prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				assertTrue(prettyPgm.toString().length()> 0);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				// the program should have 5 rules
				assertEquals(5, prog.getChildren().size());

			} catch (NoMaybeValue ex) {
				fail("Applying remove mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testRemoveMutationForBinaryCondition() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[2]);
		InputStream is = new FileInputStream(critter_programs[2]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			StringBuilder prettyPgm = new StringBuilder();
			prog.prettyPrint(prettyPgm);
			System.out.println(prettyPgm);
			Maybe<Node> mn = prog.findNodeOfType(NodeCategory.BINARY_CONDITION);
			assertTrue(mn.isPresent());
			try {
				ConditionBinary bcon = (ConditionBinary) mn.get();
				Mutation rm = MutationFactory.getRemove();
				Maybe<Program> mp = rm.apply(prog, bcon);
				assertTrue(mp.isPresent());
				prog = mp.get();
				// the program should not have binary condition
				mn = prog.findNodeOfType(NodeCategory.BINARY_CONDITION);
				assertFalse(mn.isPresent());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying remove mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testRemoveMutationForCommand() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[14]);
		InputStream is = new FileInputStream(critter_programs[14]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			Rule rul = (Rule) prog.getChildren().get(0);
			// the rule should have 2 commands
			assertEquals(3, rul.cmdCnt());
			StringBuilder prettyPgm = new StringBuilder();
			prog.prettyPrint(prettyPgm);
			System.out.println(prettyPgm);
			Maybe<Node> mn = prog.findNodeOfType(NodeCategory.UPDATE);
			assertTrue(mn.isPresent());
			try {
				Cmd cmd = (Cmd) mn.get();
				Mutation rm = MutationFactory.getRemove();
				Maybe<Program> mp = rm.apply(prog, cmd);
				assertTrue(mp.isPresent());
				prog = mp.get();
				// the rule should have 1 command
				rul = (Rule) prog.getChildren().get(0);
				assertEquals(2, rul.cmdCnt());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying remove mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testDuplicateMutationForRule() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[0]);
		InputStream is = new FileInputStream(critter_programs[0]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.RULE);
			assertEquals(1, dft.size());
			try {
				Mutation dm = MutationFactory.getDuplicate();
				Maybe<Program> mp = dm.apply(prog, prog);
				assertTrue(mp.isPresent());
				prog = mp.get();
				dft = new ArrayList<>();
				getAllNodesOfType(prog, dft, NodeCategory.RULE);
				assertEquals(2, dft.size());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				StringBuilder prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying remove mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testDuplicateMutationForCommand() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[14]);
		InputStream is = new FileInputStream(critter_programs[14]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.UPDATE);
			assertEquals(3, dft.size());
			Maybe<Node> mr = prog.findNodeOfType(NodeCategory.RULE);
			assertTrue(mr.isPresent());
			try {
				Rule rc = (Rule) mr.get();
				Mutation dm = MutationFactory.getDuplicate();
				Maybe<Program> mp = dm.apply(prog, rc);
				assertTrue(mp.isPresent());
				prog = mp.get();
				dft = new ArrayList<>();
				getAllNodesOfType(prog, dft, NodeCategory.UPDATE);
				assertEquals(4, dft.size());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				StringBuilder prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying remove mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testReplaceMutationForBinaryCondition() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[15]);
		InputStream is = new FileInputStream(critter_programs[15]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.BINARY_CONDITION);
			assertEquals(1, dft.size());
			Maybe<Node> mb = prog.findNodeOfType(NodeCategory.BINARY_CONDITION);
			assertTrue(mb.isPresent());
			try {
				ConditionBinary bcon = (ConditionBinary) mb.get();
				Mutation dr = MutationFactory.getReplace();
				Maybe<Program> mp = dr.apply(prog, bcon);
				assertTrue(mp.isPresent());
				prog = mp.get();
				dft = new ArrayList<>();
				getAllNodesOfType(prog, dft, NodeCategory.BINARY_CONDITION);
				assertEquals(0, dft.size());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				StringBuilder prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying replace mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testReplaceMutationForBinaryOperation() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[15]);
		InputStream is = new FileInputStream(critter_programs[15]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.BINARY_OPERATOR);
			assertEquals(2, dft.size());
			Maybe<Node> mbop = prog.findNodeOfType(NodeCategory.BINARY_OPERATOR);
			assertTrue(mbop.isPresent());
			try {
				ExprBinary bop = (ExprBinary) mbop.get();
				Mutation dr = MutationFactory.getReplace();
				Maybe<Program> mp = dr.apply(prog, bop);
				assertTrue(mp.isPresent());
				prog = mp.get();
				dft = new ArrayList<>();
				getAllNodesOfType(prog, dft, NodeCategory.BINARY_OPERATOR);
				assertEquals(1, dft.size());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				StringBuilder prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying replace mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testInsertMutationForBinaryCondition() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[15]);
		InputStream is = new FileInputStream(critter_programs[15]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.BINARY_CONDITION);
			assertEquals(1, dft.size());
			Maybe<Node> mbcon = prog.findNodeOfType(NodeCategory.BINARY_CONDITION);
			assertTrue(mbcon.isPresent());
			try {
				ConditionBinary bop = (ConditionBinary) mbcon.get();
				Mutation mi = MutationFactory.getInsert();
				Maybe<Program> mp = mi.apply(prog, bop);
				assertTrue(mp.isPresent());
				prog = mp.get();
				dft = new ArrayList<>();
				getAllNodesOfType(prog, dft, NodeCategory.BINARY_CONDITION);
				assertEquals(2, dft.size());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				StringBuilder prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying insert mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testInsertMutationForRelation() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[15]);
		InputStream is = new FileInputStream(critter_programs[15]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.BINARY_CONDITION);
			assertEquals(1, dft.size());
			Maybe<Node> mrel = prog.findNodeOfType(NodeCategory.RELATION);
			assertTrue(mrel.isPresent());
			try {
				ConditionRelation rel = (ConditionRelation) mrel.get();
				Mutation mi = MutationFactory.getInsert();
				Maybe<Program> mp = mi.apply(prog, rel);
				assertTrue(mp.isPresent());
				prog = mp.get();
				dft = new ArrayList<>();
				getAllNodesOfType(prog, dft, NodeCategory.BINARY_CONDITION);
				assertEquals(2, dft.size());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				StringBuilder prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying insert mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testInsertMutationForBinaryOperation() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[15]);
		InputStream is = new FileInputStream(critter_programs[15]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.BINARY_OPERATOR);
			assertEquals(2, dft.size());
			Maybe<Node> mbop = prog.findNodeOfType(NodeCategory.BINARY_OPERATOR);
			assertTrue(mbop.isPresent());
			try {
				ExprBinary bop = (ExprBinary) mbop.get();
				Mutation mi = MutationFactory.getInsert();
				Maybe<Program> mp = mi.apply(prog, bop);
				assertTrue(mp.isPresent());
				prog = mp.get();
				dft = new ArrayList<>();
				getAllNodesOfType(prog, dft, NodeCategory.BINARY_OPERATOR);
				assertEquals(3, dft.size());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				StringBuilder prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying insert mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testInsertMutationForExpr() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[15]);
		InputStream is = new FileInputStream(critter_programs[15]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			List<Node> dft = new ArrayList<>();
			getAllNodesOfType(prog, dft, NodeCategory.BINARY_OPERATOR);
			assertEquals(2, dft.size());
			Maybe<Node> mbop = prog.findNodeOfType(NodeCategory.SENSOR);
			assertTrue(mbop.isPresent());
			try {
				ExprSensor fac = (ExprSensor) mbop.get();
				Mutation mi = MutationFactory.getInsert();
				Maybe<Program> mp = mi.apply(prog, fac);
				assertTrue(mp.isPresent());
				prog = mp.get();
				dft = new ArrayList<>();
				getAllNodesOfType(prog, dft, NodeCategory.BINARY_OPERATOR);
				assertEquals(3, dft.size());
				// pretty print the mutated program
				// re-parse to make sure that it is valid
				StringBuilder prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
				r = new StringReader(prettyPgm.toString());
				prog = parser.parse(r);
				assertNotNull(prog);
				prettyPgm = new StringBuilder();
				prog.prettyPrint(prettyPgm);
				System.out.println(prettyPgm);
			} catch (NoMaybeValue ex) {
				fail("Applying insert mutation to program failed");
			}
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testRandomMutationsForProgram() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[15]);
		InputStream is = new FileInputStream(critter_programs[15]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			//run mutate a few times
			for (int i=0; i<5; i++){
				Program mutatedProg = prog.mutate();
				//the mutated program should be valid
				if (mutatedProg != null) {
					StringBuilder prettyPgm = new StringBuilder();
					mutatedProg.prettyPrint(prettyPgm);
					System.out.println(prettyPgm);
					r = new StringReader(prettyPgm.toString());
					mutatedProg = parser.parse(r);
					assertNotNull(mutatedProg);
				}
			}
		}
		catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testProgramClone() throws FileNotFoundException {
		System.out.println("Critter program: " + critter_programs[15]);
		InputStream is = new FileInputStream(critter_programs[15]);
		Reader r = new BufferedReader(new InputStreamReader(is));
		Parser parser = ParserFactory.getParser();
		try {
			Program prog = parser.parse(r);
			assertNotNull(prog);
			//clone the program
			Program pc = (Program) prog.clone();
			//the clone and the source
			//be exactly the same
			StringBuilder prettyPgm = new StringBuilder();
			prog.prettyPrint(prettyPgm);
			System.out.println("Source program: "+ prettyPgm);
			StringBuilder prettyPgmClone = new StringBuilder();
			pc.prettyPrint(prettyPgmClone);
			System.out.println("Clone program: "+ prettyPgmClone);
			assertEquals(prettyPgm.toString(), prettyPgmClone.toString());

		}
		catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}

	private void getAllNodesOfType(Node node, List<Node> dft, NodeCategory type) {
		int size = node.size();
		for (int i=0; i<size; i++) {
			Node child = node.nodeAt(i);
			if (child.getCategory() == type) {
				dft.add(child);
			}
		}
	}
}
