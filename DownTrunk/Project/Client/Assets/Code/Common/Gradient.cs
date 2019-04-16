using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{

	[AddComponentMenu("UI/Effects/Gradient")]
	public class Gradient : BaseMeshEffect
	{
		[SerializeField] public Color32 topColor = Color.white;

		[SerializeField] public Color32 bottomColor = Color.black;

		[SerializeField] public bool useGraphicAlpha = true;    //����Alpha

		public override void ModifyMesh(VertexHelper vh)
		{
			if (!IsActive())
			{
				return;
			}

			var vertexList = new List<UIVertex>();
			vh.GetUIVertexStream(vertexList);
			int count = vertexList.Count;

			ApplyGradient(vertexList, 0, count);
			vh.Clear();
			vh.AddUIVertexTriangleStream(vertexList);
		}

		private void ApplyGradient(List<UIVertex> vertexList, int start, int end)
		{
			if (vertexList == null || vertexList.Count == 0) return;
			float bottomY = vertexList[0].position.y;
			float topY = vertexList[0].position.y;
			for (int i = start; i < end; ++i)
			{
				float y = vertexList[i].position.y;
				if (y > topY)
				{
					topY = y;
				}
				else if (y < bottomY)
				{
					bottomY = y;
				}
			}

			//float uiElementHeight = topY - bottomY;
			//for (int i = start; i < end; ++i)
			//{
			//    UIVertex uiVertex = vertexList[i];
			//    uiVertex.color = Color32.Lerp(bottomColor, topColor, (uiVertex.position.y - bottomY) / uiElementHeight);
			//    vertexList[i] = uiVertex;
			//}

			for (int i = start; i < end; ++i)
			{
				ChangeColor(ref vertexList, i, topColor);
				ChangeColor(ref vertexList, i + 1, topColor);
				ChangeColor(ref vertexList, i + 2, bottomColor);
				ChangeColor(ref vertexList, i + 3, bottomColor);
				ChangeColor(ref vertexList, i + 4, bottomColor);
				ChangeColor(ref vertexList, i + 5, topColor);

				i += 5;
			}
		}

		private void ChangeColor(ref List<UIVertex> verList, int i, Color32 color)
		{
			UIVertex uiVertex = verList[i];
			if (useGraphicAlpha)
			{
				uiVertex.color = new Color32(color.r, color.g, color.b, uiVertex.color.a);
			}
			else
			{
				uiVertex.color = color;
			}
			verList[i] = uiVertex;
		}
	}
}
