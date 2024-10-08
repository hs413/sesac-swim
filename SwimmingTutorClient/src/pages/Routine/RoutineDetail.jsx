import { useEffect, useState, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import SessionData from '../../components/Routine/SessionForm.jsx';
import { customAxios } from '../../utils/https/axios/customAxios';
import usePageSetup from '../../hooks/usePageSetup.js';
import PageTitle from '../../components/PageTitle.jsx';
import Button from '../../components/UI/Button.jsx';
import BLANKDIV from '../../constants/blankDiv.js';

const RoutineDetailPage = () => {
  usePageSetup('routine-routineNo');

  const navigate = useNavigate();
  const { routineNo } = useParams();

  const [routineInfo, setRoutineInfo] = useState({
    routineName: '',
    poolLength: 0,
    selStrokes: '',
    targetDistance: 0,
    created: '',
    updated: '',
    trainingData: []
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await customAxios.get(`/routine/${routineNo}`);
        setRoutineInfo(response.data);
      } catch (error) {
        console.error(error);
      }
    };
    fetchData();
  }, [routineNo]);

  const { trainingData } = routineInfo;
  const sessionName = ['워밍업', '코어', '쿨다운'];
  const categorizedData = sessionName.map(name => trainingData.filter(training => training.session === name));

  const formatDate = useMemo(
    () => date => {
      const formattedDate = new Date(date).toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
      return formattedDate;
    },
    []
  );

  const handleDelete = async () => {
    // Request to the server with customAxios
    try {
      await customAxios.delete(`/routine/${routineNo}`);
      // Redirect to /routine page
      navigate('/routine');
    } catch (error) {
      console.error('Error deleting data:', error);
    }
  };

  const handleUpdate = async () => {
    // Request to the server with customAxios
    try {
      navigate('/routine/update', {
        state: {
          routineNo: routineNo,
          routineName: routineInfo.routineName,
          poolLength: routineInfo.poolLength,
          targetDistance: routineInfo.targetDistance,
          selStrokes: routineInfo.selStrokes
        }
      });
    } catch (error) {
      console.error('Error posting data:', error);
    }
  };

  const InfoRow = ({ label, value }) => (
    <tr>
      <td className='border-b px-2 py-1 text-center font-semibold'>{label}</td>
      <td className='border-b px-2 py-1 text-center'>{value}</td>
    </tr>
  );

  return (
    <div className='px-7'>
      <PageTitle title={`${routineInfo.routineName}`} />
      {BLANKDIV[3]}
      <table className='min-w-full border border-gray-300 bg-white'>
        <tbody>
          <InfoRow label='레인길이' value={`${routineInfo.poolLength}m`} />
          <InfoRow label='훈련영법' value={routineInfo.selStrokes} />
          <InfoRow label='목표거리' value={`${routineInfo.targetDistance}m`} />
          <InfoRow label='생성일시' value={formatDate(routineInfo.created)} />
          <InfoRow label='수정일시' value={formatDate(routineInfo.updated)} />
        </tbody>
      </table>
      {BLANKDIV[1]}
      <div className='mt-4 flex justify-between'>
        <Button key='delete' onClick={handleDelete} content='삭제' type='cancel' />
        <Button key='update' onClick={handleUpdate} content='수정' />
      </div>
      {BLANKDIV[3]}
      <div className='grid gap-3'>
        {sessionName.map((name, index) => (
          <SessionData key={name} title={name} data={categorizedData[index]} />
        ))}
      </div>
    </div>
  );
};

export default RoutineDetailPage;
