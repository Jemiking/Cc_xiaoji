import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { HomeScreen } from './src/screens/Home/HomeScreen';
import { AccountingScreen } from './src/screens/Accounting/AccountingScreen';
import { MemoScreen } from './src/screens/Memo/MemoScreen';
import { TodoScreen } from './src/screens/Todo/TodoScreen';
import { colors } from './src/styles/theme';
import type { NativeStackNavigationOptions } from '@react-navigation/native-stack';

export type RootStackParamList = {
  Home: undefined;
  Accounting: undefined;
  Memo: undefined;
  Todo: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

const screenOptions: NativeStackNavigationOptions = {
  headerStyle: {
    backgroundColor: colors.primary,
  },
  headerTintColor: colors.text,
  headerTitleStyle: {
    fontWeight: '700',
  },
};

export default function App() {
  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <Stack.Navigator
          initialRouteName="Home"
          screenOptions={screenOptions}
        >
          <Stack.Screen 
            name="Home" 
            component={HomeScreen}
            options={{
              title: '小记',
            }}
          />
          <Stack.Screen 
            name="Accounting" 
            component={AccountingScreen}
            options={{
              title: '记账',
            }}
          />
          <Stack.Screen 
            name="Memo" 
            component={MemoScreen}
            options={{
              title: '备忘录',
            }}
          />
          <Stack.Screen 
            name="Todo" 
            component={TodoScreen}
            options={{
              title: '待办事项',
            }}
          />
        </Stack.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
} 