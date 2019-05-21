using UnityEngine;
using System.Collections;

public class SetSortingOrder : MonoBehaviour {

    // 深度遍历设置所有 Render 的 SortingLayer
    public static void SetRenderSortingLayerByActor(GameObject actor, int order, bool isRecursion)
    {
        if (null != actor)
        {
            SetSpriteRenderSortingLayerByActor(actor, order);
            SetParticleSystemSortingLayerByActor(actor, order);
            SetMeshRenderSortingLayerByActor(actor, order);

            int childCount = actor.transform.childCount;
            int idx = 0;
            Transform childTrans = null;

            if (isRecursion)
            {
                for (idx = 0; idx < childCount; ++idx)
                {
                    childTrans = actor.transform.GetChild(idx);
                    SetRenderSortingLayerByActor(childTrans.gameObject, order, isRecursion);
                }
            }
        }
    }

    //SpriteRenderer
    static public void SetSpriteRenderSortingLayerByActor(UnityEngine.GameObject actor, int order)
    {
        if (null != actor)
        {
            SpriteRenderer render = null;
            render = actor.GetComponent<SpriteRenderer>();

            SetSpriteRenderSortingLayerBySpriteRenderer(render, order);
        }
    }

    static public void SetSpriteRenderSortingLayerBySpriteRenderer(SpriteRenderer render, int order)
    {
        if (null != render && render.sortingOrder != order)
        {
            render.sortingOrder = order;
        }
    }

    //Particle
    static public void SetParticleSystemSortingLayerByActor(UnityEngine.GameObject actor, int order)
    {
        if (null != actor)
        {
            ParticleSystem particleSystem = null;
            particleSystem = actor.GetComponent<ParticleSystem>();

            SetParticleSystemSortingLayer(particleSystem, order);
        }
    }

    static public void SetParticleSystemSortingLayer(ParticleSystem particleSystem, int order)
    {
        if (null != particleSystem)
        {
            Renderer render = particleSystem.GetComponent<Renderer>();
            if (null != render && render.sortingOrder != order)
            {
                render.sortingOrder = order;
            }
        }
    }


    //Mesh
    static public void SetMeshRenderSortingLayerByActor(UnityEngine.GameObject actor, int order)
    {
        if (null != actor)
        {
            UnityEngine.MeshRenderer meshRenderer = null;
            meshRenderer = actor.GetComponent<UnityEngine.MeshRenderer>();

            SetMeshRenderSortingLayer(meshRenderer, order);
        }
    }

    static public void SetMeshRenderSortingLayer(UnityEngine.MeshRenderer meshRenderer, int order)
    {
        if (null != meshRenderer && meshRenderer.sortingOrder != order)
        {
            meshRenderer.sortingOrder = order;
        }
    }
}
