using UnityEngine;
using System.Collections.Generic;
using System.Collections;
using System;

namespace StringToTree
{
	public class Node
	{
		public Node LeftNode;
		public Node RightNode;

		public virtual float GetTreeSum (float[] nums)
		{
			return 0;
		}
	}

	//常数节点--
	public class ConstantNode : Node
	{
		public float SumNum;

		public ConstantNode(float a)
		{
			SumNum = a;
		}

		public override float GetTreeSum(float[] nums)
		{
			//Debug.Log(SumNum);
			return SumNum;
		}
	}
	//变量节点--
	public class VariableNode : Node
	{
		public int iSubScript;	//下标--

		public VariableNode(int num)
		{
			iSubScript = num;
		}

		public override float GetTreeSum(float[] nums)
		{
			if (iSubScript > nums.Length)
			{
				Debug.LogError("The number of parameter error");
			}
			//Debug.Log(nums[iSubScript] + "   " + iSubScript);
			return nums[iSubScript];
		}
	}

	//加法节点--
	public class AddNode : Node
	{
		public AddNode(Node Left,Node Right)
		{
			LeftNode = Left;
			RightNode = Right;
		}

		public override float GetTreeSum(float[] nums)
		{
			float a = LeftNode.GetTreeSum (nums);
			float b = RightNode.GetTreeSum (nums);
			//Debug.Log(a +"+"+b);
			return a + b;
		}
	}
	//减法节点--
	public class SubtractNode : Node
	{
		public SubtractNode(Node Left,Node Right)
		{
			LeftNode = Left;
			RightNode = Right;
		}

		public override float GetTreeSum(float[] nums)
		{
			float a = LeftNode.GetTreeSum (nums);
			float b = RightNode.GetTreeSum (nums);
			//Debug.Log(a +"-"+b);
			return a - b;
		}
	}

	//乘法节点--
	public class MultiplyNode : Node
	{
		public MultiplyNode(Node Left,Node Right)
		{
			LeftNode = Left;
			RightNode = Right;
		}

		public override float GetTreeSum(float[] nums)
		{			
			float a = LeftNode.GetTreeSum (nums);
			float b = RightNode.GetTreeSum (nums);
			//Debug.Log(a +"*"+b);
			return a * b;
		}
	}

	//除法节点--
	public class DivideNode : Node
	{
		public DivideNode(Node Left,Node Right)
		{
			LeftNode = Left;
			RightNode = Right;
		}

		public override float GetTreeSum(float[] nums)
		{
			float a = LeftNode.GetTreeSum (nums);
			float b = RightNode.GetTreeSum (nums);
			//Debug.Log(a +"/"+b);
			return a / b;
		}
	}

	public class AnalysisFormula
	{
		public static Node Build_BinaryTree(string sFormula, string[] formulaParam)
		{
			Stack stack = new Stack();
			string[] strTemp = sFormula.Split(',');
			Node rightNode = null;
			Node LeftNode = null;
			for (int i=0, Max = strTemp.Length; i<Max; ++i)
			{
				string str = strTemp[i];
				if (str.Length == 1)
				{
					switch (str)
					{
					case "+":
						if(stack.Count>1)
						{
							rightNode = stack.Pop() as Node;
							LeftNode = stack.Pop() as Node;
							AddNode addNode = new AddNode(LeftNode,rightNode);
							stack.Push(addNode);
						}
						else
							Debug.LogError("Error");

						continue;
					case "-":
						if(stack.Count>1)
						{
							rightNode = stack.Pop() as Node;
							LeftNode = stack.Pop() as Node;
							SubtractNode SubNode = new SubtractNode(LeftNode,rightNode);
							stack.Push(SubNode);
						}
						else if(stack.Count == 1){
							rightNode = stack.Pop() as Node;
							LeftNode = new ConstantNode(-1f);
							MultiplyNode MulNode = new MultiplyNode(LeftNode,rightNode);
							stack.Push(MulNode);
						}else
							Debug.LogError("Error");
						continue;
					case "*":
						if(stack.Count>1)
						{
							rightNode = stack.Pop() as Node;
							LeftNode = stack.Pop() as Node;
							MultiplyNode MulNode = new MultiplyNode(LeftNode,rightNode);
							stack.Push(MulNode);
						}
						else
							Debug.LogError("Error");

						continue;
					case "/":
						if(stack.Count>1)
						{
							rightNode = stack.Pop() as Node;
							LeftNode = stack.Pop() as Node;
							DivideNode DivNode = new DivideNode(LeftNode,rightNode);
							stack.Push(DivNode);
						}
						else
							Debug.LogError(i + "Error");

						continue;
					}
				}

				char a = str[0];
				if(a >= '0' && a <= '9')
				{
					ConstantNode node = new ConstantNode(float.Parse(str));
					stack.Push(node);
				}
				else
				{
					int index = GetIndex(formulaParam,str);
					VariableNode node = new VariableNode(index);
					stack.Push(node);
				}
			}

			Node EndNode = stack.Pop() as Node;
			stack.Clear();
			stack = null;
			return EndNode;
		}

		private static int GetIndex(string[] formulaParam,string str)
		{
			for (int i=0,Max = formulaParam.Length; i<Max; ++i)
			{
				if(formulaParam[i].Equals(str))
				{
					return i;
				}
			}
			Debug.LogError("Can`t find " + str);
			return 0;
		}
	}
}
