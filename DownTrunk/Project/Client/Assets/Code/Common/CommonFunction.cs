using MS;
using System;
using System.Collections;
using System.Text;
using System.Text.RegularExpressions;
using UnityEngine;

public class CommonFunction
{
	public static string FormatNumber(int num)
	{
		if(num >= 100000000)
			return string.Format("{0}{1}", num / 100000000, ConfigData.GetStaticText("10901"));
		else if(num >= 100000)
			return string.Format("{0}{1}", num / 10000, ConfigData.GetStaticText("10900"));
		else
			return num.ToString();
	}

	public static string FormatNumber(string num)
	{
		int n = int.Parse(num);
		if(n >= 1000000000)
			return string.Format("{0}{1}", n / 100000000, ConfigData.GetStaticText("10901"));
		else if(n >= 100000)
			return string.Format("{0}{1}", n / 10000, ConfigData.GetStaticText("10900"));
		else
			return num;
	}


    public static IEnumerator DelayTimeFunc(Action action, float delaySeconds)
    {
        yield return new WaitForSeconds(delaySeconds);
        action();
    }


    public static void InitAnimationEvent(Animation anim,string animName, string funcName, float time = 0)
    {
        AnimationState animState = anim[animName];
        AnimationEvent e = new AnimationEvent();
        if (time == 0)
            e.time = animState.length;
        else
            e.time = time;
        e.functionName = funcName;
        animState.clip.AddEvent(e);
    }

	public static string WrapWord(string originalWord, int lineWidth)
	{
		StringBuilder sb = new StringBuilder();
		Regex punctuationRegex = new Regex(@"[，。；？~！：‘“”’【】（）]");
		int lastSpace = 0;
		int lastCharacter = 0;
		bool bCharacter = false;
		int tempNum = 0;
		int appendN = 0;
		char[] c = originalWord.ToCharArray();
		for (int i = 0; i < c.Length; i++)
		{
			if(c[i] == '\n')
			{
				sb.Append(c[i]);
				tempNum = 0;
				continue;
			}

			if(c[i] == '<')
			{
				while(c[i] != '>')
				{
					sb.Append(c[i++]);
					if(i >= c.Length)
						break;
				}
				if(i >= c.Length)
					break;
				sb.Append(c[i]);
				continue;
			}

			if(c[i] == ' ')
				lastSpace = i + appendN;

			if((c[i] >= 0x4e00 && c[i] <= 0x9fa5) || punctuationRegex.IsMatch(c[i].ToString()))
			{
				bCharacter = true;
				tempNum += 2;
				if(tempNum > lineWidth)
				{
					i--;
					sb.Append("\n");
					++appendN;
					tempNum = 0;
					lastSpace = 0;
				}
				else
					sb.Append(c[i]);
			}
			else
			{
				if(bCharacter)
					lastCharacter = i;
				bCharacter = false;

				tempNum++;
				if(tempNum > lineWidth)
				{
					int tempCount = 0;
					if(c[i] == ' ')
					{
						c[i] = '\n';
						tempCount = i;
					}
					else
					{
						if(lastCharacter > lastSpace)
						{
							sb.Insert(lastCharacter, '\n');
							tempCount = lastCharacter;
						}
						else if(lastCharacter < lastSpace || lastSpace != 0)
						{
							sb[lastSpace] = '\n';
							tempCount = lastSpace;
						}
					}
							
					sb.Append(c[i]);
					tempNum = i - tempCount;
					lastSpace = 0;
				}
				else
					sb.Append(c[i]);
			}
		}
		return sb.ToString();
	}
}
