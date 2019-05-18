using UnityEngine;

namespace MS
{
	public class BattleSkillBtn : MonoBehaviour
	{
		public Transform m_Transform;
		private Animation _anim;
		private Vector3 _vecTemp = new Vector3();
		private Vector3 _oriScale = new Vector3(1.5f, 1.5f, 1f);
		private Ray _ray;
		private RaycastHit _hit;

		public int Type { get; set; }

		private void Awake()
		{
			m_Transform = transform;
			_anim = gameObject.GetComponent<Animation>();
		}

		private void OnMouseDown()
		{
			_anim["SkillBtn2"].time = 0;
			_anim["SkillBtn2"].speed = 1;
			_anim.Play("SkillBtn2");
		}

		private void OnMouseUp()
		{
			_anim["SkillBtn2"].time = _anim["SkillBtn2"].length;
			_anim["SkillBtn2"].speed = -1;
			_anim.Play("SkillBtn2");
			_ray = BattleManager.GetInst().BattleCam.ScreenPointToRay(Input.mousePosition);
			if(Physics.Raycast(_ray, out _hit, 1000, 1 << LayerMask.NameToLayer("SkillBtn")))
			{
				//int playerIndex = int.Parse(_hit.collider.tag);
			}
		}

		public void SetToOriScale()
		{
			m_Transform.localScale = _oriScale;
		}

		public void SetPosImmediately(float y)
		{
			_vecTemp.y = y;
			m_Transform.localPosition = _vecTemp;
		}
	}
}
